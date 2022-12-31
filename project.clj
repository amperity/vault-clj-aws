(defproject amperity/vault-clj-aws "0.0.5-SNAPSHOT"
  :description "Extension to com.amperity/vault-clj that adds the AWS auth method."
  :url "https://github.com/amperity/vault-clj-aws"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :deploy-branches ["master"]
  :pedantic? :abort

  :dependencies
  [[org.clojure/clojure "1.11.1"]
   [org.clojure/tools.logging "1.2.4"]
   [amperity/envoy "1.0.1"]
   [com.amperity/vault-clj "2.0.0-SNAPSHOT"]

   ;; External dependencies
   [com.amazonaws/aws-java-sdk-core "1.12.230"
    :exclusions [org.apache.httpcomponents/httpclient
                 org.apache.httpcomponents/httpcore]]
   [commons-logging "1.2"]]

  :profiles
  {:repl
   {:source-paths ["dev"]
    :dependencies
    [[org.clojure/tools.namespace "1.3.0"]]}})
