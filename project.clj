(defproject amperity/vault-clj-aws "0.0.1"
  :description "Vault-clj extension to support aws iam instance authentication."
  :url "https://github.com/amperity/vault-clj-aws"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :deploy-branches ["master"]
  :pedantic? :abort

  :dependencies
  [[org.clojure/clojure "1.9.0"]
   [amperity/vault-clj "0.6.6"]

   ; External dependencies
   [cheshire "5.8.0"]
   [buddy/buddy-core "1.4.0"]
   [com.amazonaws/aws-java-sdk-core "1.11.435"]]

  :profiles
  {:repl
   {:source-paths ["dev"]
    :dependencies
    [[org.clojure/tools.namespace "0.3.0-alpha4"]]}})
