(defproject com.amperity/vault-clj-aws "2.0.0"
  :description "Vault-clj extension that implements the AWS auth method."
  :url "https://github.com/amperity/vault-clj-aws"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :deploy-branches ["master"]

  :dependencies
  [[org.clojure/clojure "1.12.2"]

   [com.amperity/vault-clj "2.3.588"]
   [org.clojure/data.json "2.5.1"]

   ;; External dependencies
   [software.amazon.awssdk/auth "2.40.13"]
   [software.amazon.awssdk/http-auth-aws "2.40.13"]]

  :profiles
  {:repl
   {:source-paths ["dev"]}})
