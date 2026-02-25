(ns user
  (:require
    [vault.auth.aws :as aws]
    [vault.client :as vault])
  (:import
    (software.amazon.awssdk.auth.credentials
      AwsSessionCredentials)))


(def vault-client
  nil)


(defn start
  []
  (alter-var-root #'vault-client (constantly (vault/new-client (System/getenv "VAULT_ADDR"))))
  (alter-var-root #'vault-client vault/start))


(defn stop
  []
  (alter-var-root #'vault-client vault/stop)
  (alter-var-root #'vault-client (constantly nil)))


(comment
  (def session-credentials
    (AwsSessionCredentials/create
      "access-key-id"
      "secret-access-key"
      "session-token"))
  (aws/login
    vault-client
    {:aws-region "us-east-1"
     :aws-credentials session-credentials}))
