(ns vault.auth.ext.aws
  "The AWS authentication backend allows a caller with AWS
  credentials to obtain a Vault token, and is mounted at /auth/aws by default.

  Reference: <https://developer.hashicorp.com/vault/docs/auth/aws>"
  (:require
    [clojure.data.json :as json]
    [vault.client.http :as http]
    [vault.client.proto :as proto]
    [vault.util :as u])
  (:import
    (com.amazonaws
      DefaultRequest
      SignableRequest)
    (com.amazonaws.auth
      AWS4Signer
      DefaultAWSCredentialsProviderChain)
    (com.amazonaws.http
      HttpMethodName)
    com.amazonaws.regions.DefaultAwsRegionProviderChain
    java.io.ByteArrayInputStream
    (java.net
      URI)
    (java.util
      Base64)
    vault.client.http.HTTPClient))


(def default-mount
  "Default mount point to use if one is not provided."
  "aws")


(def default-sts-region
  "Default AWS region for STS."
  "us-east-1")


(defprotocol API
  "The AWS endpoints manage AWS authentication functionality."

  (with-mount
    [client mount]
    "Return an updated client which will resolve calls against the provided
    mount instead of the default. Passing `nil` will reset the client to the
    default.")

  (login
    [client params]
    "Calls the Vault [AWS login endpoint](https://developer.hashicorp.com/vault/api-docs/auth/aws#login)
    to authenticate the client.

    Returns the `auth` map from the login endpoint, and updates the auth information
    in the client, including the new client token.

    AWS credentials are obtained from the AWS Java SDK's default credentials
    provider chain, `com.amazonaws.auth.DefaultAWSCredentialsProviderChain`.

    The AWS region is obtained from the AWS Java SDK's default region provider
    chain, `com.amazonaws.regions.DefaultAwsRegionProviderChain`.

    `params` accepts these parameters per the Vault documentation:

    * `:role`
    * `:identity`
    * `:signature`
    * `:pkcs7`
    * `:nonce`

    The following parameters values are automatically generated and cannot
    be passed into this method:

    * `:iam_request_url`
    * `:iam_http_request_method`
    * `:iam_request_headers`
    * `:iam_request_body`"))


(defn- sts-get-caller-identity-request
  "Create a GetCallerIdentity request ready for signing."
  ^SignableRequest
  []
  (let [body (.getBytes "Action=GetCallerIdentity&Version=2011-06-15")]
    (doto (DefaultRequest. "sts")
      (.setEndpoint (URI. "https://sts.amazonaws.com"))
      (.setResourcePath "/")
      (.setHttpMethod HttpMethodName/POST)
      (.addHeader "Content-Type" "application/x-www-form-urlencoded; charset=utf-8")
      (.addHeader "Content-Length" (str (count body)))
      (.setContent (ByteArrayInputStream. body)))))


(defn- b64encode
  [^String s]
  (.encodeToString (Base64/getEncoder) (.getBytes s "UTF-8")))


(defn- make-request-body
  "Returns an HTTP request body map for the AWS login endpoint."
  [aws-region aws-creds login-params]
  (let [sts-request (sts-get-caller-identity-request)
        signer (doto (AWS4Signer.)
                 (.setServiceName "sts")
                 (.setRegionName aws-region))]
    ;; Mutates the AWS request object in place, setting correct Authorization
    ;; with signature.
    (.sign signer sts-request aws-creds)
    (merge
      {:iam_http_request_method (str (.getHttpMethod sts-request))
       :iam_request_url (-> (str (.getEndpoint sts-request) (.getResourcePath sts-request))
                            (b64encode))
       :iam_request_body (-> (.getContent sts-request)
                             (slurp)
                             (b64encode))
       :iam_request_headers (-> (.getHeaders sts-request)
                                (json/write-str)
                                (b64encode))}
      (select-keys
        login-params
        [:role
         :identity
         :signature
         :pkcs7
         :nonce]))))


(extend-type HTTPClient

  API

  (with-mount
    [client mount]
    (if (some? mount)
      (assoc client ::mount mount)
      (dissoc client ::mount)))

  (login
    [client params]
    (let [mount (::mount client default-mount)
          api-path (u/join-path "auth" mount "login")
          aws-region (.getRegion (DefaultAwsRegionProviderChain.))
          aws-creds (.getCredentials (DefaultAWSCredentialsProviderChain.))
          request-body (make-request-body aws-region aws-creds params)]
      (http/call-api
        client :aws-login :post api-path
        {:content-type :json
         :body request-body
         :handle-response u/kebabify-body-auth
         :on-success (fn update-auth
                       [auth]
                       (proto/authenticate! client auth))}))))
