(ns vault.auth.ext.main
  (:gen-class)
  (:require
    [vault.auth.ext.aws :as aws]
    [vault.client.http :as http]))


(defn -main
  [& args]
  (let [vault-addr (or (System/getenv "VAULT_ADDR")
                       (throw (Exception. "VAULT_ADDR is not set!")))
        client (http/http-client vault-addr)]
    (prn (aws/login client {}))
    (prn client)))
