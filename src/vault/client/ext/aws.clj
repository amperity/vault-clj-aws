(ns vault.client.ext.aws
  (:require
    [cheshire.core :as json]
    [clj-http.client :as http]
    [clojure.java.io :as io]
    [envoy.core :refer [defenv]]
    [vault.client.http :as client])
  (:import
    (com.amazonaws
      DefaultRequest
      Request
      SignableRequest)
    (com.amazonaws.auth
      AWS4Signer
      AWSCredentials
      BasicAWSCredentials
      BasicSessionCredentials
      DefaultAWSCredentialsProviderChain)
    (com.amazonaws.http
      HttpMethodName)
    (java.net
      URI)
    (java.util
      Base64)))


(defenv :vault-aws-iam-role
  "The configured vault aws auth role used to perform instance authentication.")


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
    (.setContent (-> ^String payload (.getBytes "UTF-8") (io/input-stream)))))


(defn derive-credentials
  "AWSCredentials to authenticate with STS. Will fall back to default chain including instance profile."
  ^AWSCredentials
  ([id secret token]
   (BasicSessionCredentials. id secret token))
  ([id secret]
   (BasicAWSCredentials. id secret))
  ([]
   (-> (DefaultAWSCredentialsProviderChain.)
       (.getCredentials))))


(defn signer
  "Signing object used to perform AWS4 signing on sts request object."
  ^AWS4Signer
  []
  (doto (AWS4Signer.)
    (.setServiceName "sts")
    (.setRegionName "us-east-1")))


(defn- str->b64str
  [^java.util.Base64$Encoder b64 ^String s]
  (-> s
      (.getBytes "UTF-8")
      (->> (.encodeToString b64))))


(defn- request-parameters
  "Takes signed aws Request object to derive parameters required by vault auth backend api call.
  https://www.vaultproject.io/api/auth/aws/index.html#login"
  [^Request req]
  (let [b64encode (partial str->b64str (Base64/getEncoder))]
    {"iam_http_request_method" (str (.getHttpMethod req))
     "iam_request_url" (-> (format "%s%s"
                                   (.getEndpoint req)
                                   (.getResourcePath req))
                           (b64encode))
     "iam_request_body" (-> (.getContent req)
                            (slurp)
                            (b64encode))
     "iam_request_headers" (-> (.getHeaders req)
                               (json/encode)
                               (b64encode))}))


(defmethod client/authenticate* :aws-iam
  [client _ aws-ctx]
  (let [{:keys [iam-role credentials]} aws-ctx
        aws-creds ^AWSCredentials (or credentials (derive-credentials))
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
