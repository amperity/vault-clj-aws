(defproject amperity/vault-clj-aws "0.0.2-SNAPSHOT"
  :description "Vault-clj extension to support aws iam instance authentication."
  :url "https://github.com/amperity/vault-clj-aws"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :deploy-branches ["master"]
  :pedantic? :abort

  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [org.clojure/tools.logging "0.5.0"]
   [amperity/envoy "0.3.3"]
   [amperity/vault-clj "0.7.0"]
   [cheshire "5.9.0"]

   ; External dependencies
   [com.amazonaws/aws-java-sdk-core "1.11.604"
    :exclusions [org.apache.httpcomponents/httpclient
                 org.apache.httpcomponents/httpcore]]
   [commons-logging "1.2"]]

  :profiles
  {:repl
   {:source-paths ["dev"]
    :dependencies
    [[org.clojure/tools.namespace "0.3.1"]]}})
