(ns vault.auth.aws
  (:require
    [clojure.data.json :as json]
    [vault.client.http :as http]
    [vault.client.proto :as proto]
    [vault.util :as u])
  (:import
    java.nio.charset.StandardCharsets
    java.util.Base64
    (software.amazon.awssdk.auth.credentials
      AwsCredentials
      DefaultCredentialsProvider)
    (software.amazon.awssdk.http
      ContentStreamProvider
      SdkHttpFullRequest
      SdkHttpMethod)
    software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner
    software.amazon.awssdk.http.auth.spi.signer.SignRequest
    (software.amazon.awssdk.regions.providers
      DefaultAwsRegionProviderChain)
    vault.client.http.HTTPClient))


(defn- b64-string
  [^String string]
  (let [encoder (Base64/getEncoder)
        b64-bytes (.encode encoder (.getBytes string StandardCharsets/UTF_8))]
    (String. b64-bytes StandardCharsets/UTF_8)))


(defn- iam-login-request-body
  "Creates the body for a `login` request.
  Creates an STS GetCallerIdentity request for the given AWS region and signs
  it with the given AWS credentials."
  [^AwsCredentials aws-credentials ^String aws-region vault-role]
  (let [signer (AwsV4HttpSigner/create)
        content (.getBytes "Action=GetCallerIdentity&Version=2011-06-15" StandardCharsets/UTF_8)
        payload (ContentStreamProvider/fromByteArray content)
        request-to-sign (-> (SdkHttpFullRequest/builder)
                            (.protocol "https")
                            (.host "sts.amazonaws.com")
                            (.method SdkHttpMethod/POST)
                            (.appendHeader "Content-Type" "application/x-www-form-urlencoded; charset=utf-8")
                            (.appendHeader "Content-Length" (str (count content)))
                            (.build))
        signed-request (.sign
                         signer
                         (-> (SignRequest/builder aws-credentials)
                             (.request request-to-sign)
                             (.payload payload)
                             (.putProperty AwsV4HttpSigner/SERVICE_SIGNING_NAME "sts")
                             (.putProperty AwsV4HttpSigner/REGION_NAME aws-region)
                             (.build)))
        signed-headers (into {}
                             (map (fn [[header-key header-values]]
                                    [header-key (first header-values)]))
                             (.headers (.request signed-request)))]
    (merge
      (when vault-role
        {"role" vault-role})
      {"iam_http_request_method" (-> signed-request .request .method str)
       "iam_request_url" (-> signed-request .request .getUri str b64-string)
       "iam_request_body" (-> signed-request .payload .get .newStream slurp b64-string)
       "iam_request_headers" (-> signed-headers json/write-str b64-string)})))


(def default-mount
  "Default mount point for the AWS auth method."
  "aws")


(defprotocol API
  "Vault AWS auth method"

  (login
    [client]
    [client {:keys [role aws-region aws-credentials]}]
    "Logs in to Vault using AWS credentials
    as per <https://developer.hashicorp.com/vault/api-docs/auth/aws#login>.
    This uses the `iam` method.

    By default, uses the AWS SDK's `DefaultCredentialsProvider` to get AWS
    credentials, and `DefaultAwsRegionProviderChain` to infer the region of the
    STS service.

    Options:

    - `:role`: name of the role configured in Vault. If not provided, Vault
       infers the role name from the AWS credentials.

    - `:aws-region`: AWS region name (string) of the STS service

    - `:aws-credentials`: an `AwsCredentials` instance")

  (with-mount
    [client mount]
    "Configures the mount point of the AWS auth method, if it is not the default \"aws\".

    Returns a new client. If `mount` is nil, returns a client with the default
    mount point."))


(defn- get-default-aws-region
  []
  (str (.getRegion (.build (DefaultAwsRegionProviderChain/builder)))))


(defn- get-default-aws-credentials
  []
  (.resolveCredentials (.build (DefaultCredentialsProvider/builder))))


(extend-type HTTPClient

  API

  (login
    ([client]
     (login client nil))
    ([client {:keys [role aws-region aws-credentials]}]
     (let [mount (::mount client default-mount)
           api-path (u/join-path "auth" mount "login")
           aws-region (or aws-region (get-default-aws-region))
           aws-credentials (or aws-credentials (get-default-aws-credentials))
           body (iam-login-request-body aws-credentials aws-region role)]
       (http/call-api
         client ::login
         :post api-path
         {:info {::mount mount}
          :content-type :json
          :body body
          :handle-response u/kebabify-body-auth
          :on-success (fn update-auth
                        [auth]
                        (proto/authenticate! client auth))}))))

  (with-mount
    [client mount]
    (if (some? mount)
      (assoc client ::mount mount)
      (dissoc client ::mount))))
