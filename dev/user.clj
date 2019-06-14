(ns user
  (:require
    [buddy.core.codecs :refer [bytes->hex bytes->str]]
    [buddy.core.codecs.base64 :as b64]
    [cheshire.core :as json]
    [clj-http.client :as http]
    [clojure.java.io :as io]
    [clojure.tools.namespace.repl :refer [refresh]]
    [vault.client.ext.aws :as ext]
    [vault.core :as vault])
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
    (com.amazonaws.http
      HttpMethodName)
    java.net.URI))
