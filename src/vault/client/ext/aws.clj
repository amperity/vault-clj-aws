(ns vault.client.ext.aws
  (:require
    [buddy.core.codecs :refer [bytes->str]]
    [buddy.core.codecs.base64 :as b64]
    [cheshire.core :as json]
    [clj-http.client :as http]
    [clojure.java.io :as io]
    [vault.client.http :as client])
  (:import
    (com.amazonaws
      DefaultRequest
      Request
      SignableRequest)
    (com.amazonaws.auth
      AWS4Signer
      AWSCredentials
      BasicSessionCredentials
      DefaultAWSCredentialsProviderChain)
    com.amazonaws.http.HttpMethodName
    java.net.URI))


(def ^:private payload "Action=GetCallerIdentity&Version=2011-06-15")


(defn sts-get-caller-identity-request
  "Create a GetCallerIdentity request ready for signing."
  ^SignableRequest
  []
  (doto (DefaultRequest. "sts")
    (.setEndpoint (URI. "https://sts.amazonaws.com"))
    (.setResourcePath "/")
    (.setHttpMethod HttpMethodName/POST)
    (.addHeader "Content-Type" "application/x-www-form-urlencoded; charset=utf-8")
    (.addHeader "Content-Length" "43")
    (.setContent (-> payload (.getBytes "UTF-8") (io/input-stream)))))


(defn credentials
  "AWSCredentials to authenticate with STS. Will fall back to default chain including instance profile."
  ^AWSCredentials
  ([id secret token]
   (BasicSessionCredentials. id secret token))
  ([]
   (-> (DefaultAWSCredentialsProviderChain/getInstance)
       (.getCredentials))))


(defn signer
  "Signing object used to perform AWS4 signing on sts request object."
  ^AWS4Signer
  []
  (doto (AWS4Signer.)
        (.setServiceName "sts")
        (.setRegionName "us-east-1")))


(defn- request-parameters
  "Takes signed aws Request object to derive parameters required by vault auth backend api call.
  https://www.vaultproject.io/api/auth/aws/index.html#login"
  [^Request req]
  {"iam_http_request_method" (str (.getHttpMethod req))
   "iam_request_url" (-> (format "%s%s"
                                 (.getEndpoint req)
                                 (.getResourcePath req))
                         (b64/encode)
                         (bytes->str))
   "iam_request_body" (-> (.getContent req)
                          (slurp)
                          (b64/encode)
                          (bytes->str))
   "iam_request_headers" (-> (.getHeaders req)
                             (json/encode)
                             (b64/encode)
                             (bytes->str))})


(defmethod client/authenticate* :aws-iam
  [client _ aws-ctx]
  (let [{:keys [iam-role test-credentials]} aws-ctx
        aws-creds ^AWSCredentials (or test-credentials (credentials))
        request ^SignableRequest (sts-get-caller-identity-request)]
    ;; mutate in place, setting correct Authorization with signature
    (.sign (signer) request aws-creds)
    (client/api-auth!
      (str "AWS IAM Role " iam-role)
      (:auth client)
      (client/do-api-request
        :post (str (:api-url client) "/v1/auth/aws/login")
        (merge
          (:http-opts client)
          {:body (json/encode (request-parameters request))
           :accept :json
           :as :json})))))
