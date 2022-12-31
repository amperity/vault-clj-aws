(ns vault.auth.ext.aws
  "The AWS authentication backend allows a caller with AWS
  credentials to obtain a Vault token, and is mounted at /auth/aws by default.

  Reference: https://developer.hashicorp.com/vault/docs/auth/aws"
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [vault.client.http :as http]
    [vault.client.proto :as proto]
    [vault.util :as u])
  (:import
    (com.amazonaws
      DefaultRequest
      Request
      SignableRequest)
    (com.amazonaws.auth
      AWS4Signer
      DefaultAWSCredentialsProviderChain)
    (com.amazonaws.http
      HttpMethodName)
    com.amazonaws.regions.DefaultAwsRegionProviderChain
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
    [client]
    "Login to the provided role using the AWS SDK's
    default credentials provider chain (com.amazonaws.auth.DefaultAWSCredentialsProviderChain)
    and default region provider chain (com.amazonaws.regions.DefaultAwsRegionProviderChain).

    This method uses the `/auth/aws/login` endpoint by default.

    Returns the `auth` map from the login endpoint and also updates the auth
    information in the client, including the new client token."))


(def ^:private payload "Action=GetCallerIdentity&Version=2011-06-15")


(defn- sts-get-caller-identity-request
  "Create a GetCallerIdentity request ready for signing."
  ^SignableRequest
  []
  (doto (DefaultRequest. "sts")
    (.setEndpoint (URI. "https://sts.amazonaws.com"))
    (.setResourcePath "/")
    (.setHttpMethod HttpMethodName/POST)
    (.addHeader "Content-Type" "application/x-www-form-urlencoded; charset=utf-8")
    (.addHeader "Content-Length" "43")
    (.setContent (-> ^String payload (.getBytes "UTF-8") (io/input-stream)))))


(defn- b64encode
  [^String s]
  (.encodeToString (Base64/getEncoder) (.getBytes s "UTF-8")))


(defn- request-parameters
  "Takes signed aws Request object to derive parameters required by vault auth backend api call.
  https://www.vaultproject.io/api/auth/aws/index.html#login"
  [^Request req]
  {"iam_http_request_method" (str (.getHttpMethod req))
   "iam_request_url" (-> (str (.getEndpoint req) (.getResourcePath req))
                         (b64encode))
   "iam_request_body" (-> (.getContent req)
                          (slurp)
                          (b64encode))
   "iam_request_headers" (-> (.getHeaders req)
                             (json/write-str)
                             (b64encode))})


(extend-type HTTPClient

  API

  (with-mount
    [client mount]
    (if (some? mount)
      (assoc client ::mount mount)
      (dissoc client ::mount)))

  (login
    [client]
    (let [mount (::mount client default-mount)
          api-path (u/join-path "auth" mount "login")
          signer (doto (AWS4Signer.)
                   (.setServiceName "sts")
                   (.setRegionName (.getRegion (DefaultAwsRegionProviderChain.))))
          request (sts-get-caller-identity-request)]
      ;; Mutates the request object in place, setting correct Authorization with signature.
      (.sign signer request (.getCredentials (DefaultAWSCredentialsProviderChain.)))
      (http/call-api
        client :post api-path
        {:content-type :json
         :body (request-parameters request)
         :handle-response u/kebabify-body-auth
         :on-success (fn update-auth
                       [auth]
                       (proto/authenticate! client auth))}))))
