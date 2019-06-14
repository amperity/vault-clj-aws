(defproject amperity/vault-clj-aws "0.0.1-SNAPSHOT"
  :description "Vault-clj extension to support aws iam instance authentication."
  :url "https://github.com/amperity/vault-clj-aws"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :deploy-branches ["master"]
  :pedantic? :abort

  :dependencies
  [[org.clojure/clojure "1.9.0"]
   [org.clojure/tools.logging "0.4.0"]
   [amperity/envoy "0.3.1"]
   [amperity/vault-clj "0.6.6"]
   [buddy/buddy-core "1.4.0"]
   [cheshire "5.8.0"]

   ; External dependencies
   [com.amazonaws/aws-java-sdk-core "1.11.435"
    :exclusions [commons-logging org.apache.httpcomponents/httpcore]]]

  :profiles
  {:repl
   {:source-paths ["dev"]
    :dependencies
    [[org.clojure/tools.namespace "0.3.0-alpha4"]]}})
