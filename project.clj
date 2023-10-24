(defproject com.amperity/vault-clj-aws "2.0.0-SNAPSHOT"
  :description "Extension to com.amperity/vault-clj that adds the AWS auth method."
  :url "https://github.com/amperity/vault-clj-aws"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :deploy-branches ["master"]

  :dependencies
  [[org.clojure/clojure "1.11.1"]
   [com.amperity/vault-clj "2.2.586"]

   ;; External dependencies
   [com.amazonaws/aws-java-sdk-core "1.12.571"]]

  :profiles
  {;; uberjar for testing the login method on remote machines
   :uberjar
   {:target-path "target/uberjar"
    :uberjar-name "vault-clj-aws.jar"
    :main vault.auth.ext.main
    :aot :all}

   :repl
   {:source-paths ["dev"]
    :dependencies
    [[org.clojure/tools.namespace "1.3.0"]]}})
