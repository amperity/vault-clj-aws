(ns user
  (:require
    [vault.auth.aws :as aws]
    [vault.client :as vault]))


(def vault-addr
  "https://vault.dev.amperity.systems:8200")


(def vault-client
  nil)


(defn start
  []
  (alter-var-root #'vault-client (constantly (vault/new-client vault-addr)))
  (alter-var-root #'vault-client vault/start))


(defn stop
  []
  (alter-var-root #'vault-client vault/stop)
  (alter-var-root #'vault-client (constantly nil)))


(comment
  (import 'software.amazon.awssdk.auth.credentials.AwsSessionCredentials)
  (def session-credentials
    (AwsSessionCredentials/create
      "access-key-id"
      "secret-access-key"
      "session-token"))
  (aws/login
    vault-client
    {:aws-region "us-east-1"
     :aws-credentials session-credentials}))
