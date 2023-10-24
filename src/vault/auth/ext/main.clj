(ns vault.auth.ext.main
  (:gen-class)
  (:require
    [vault.client.http :as http]
    [vault.auth.ext.aws :as aws]))


(defn -main
  [& args]
  (let [vault-addr (or (System/getenv "VAULT_ADDR")
                       (throw (Exception. "VAULT_ADDR is not set!")))
        client (http/http-client vault-addr)]
    (aws/login client {})
    (prn client)))
