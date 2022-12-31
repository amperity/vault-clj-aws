(ns vault.auth.ext.aws-test
  (:require
    [clojure.test :refer [deftest is]]
    [vault.auth.ext.aws :as aws]
    [vault.client.http :as http]))


(deftest mounts
  (let [base-client (http/http-client "https://vault.com")
        mounted (aws/with-mount base-client "testmount")]
    (is (nil? (::aws/mount base-client)))
    (is (= "testmount" (::aws/mount mounted)))
    (is (nil? (::aws/mount (aws/with-mount mounted nil))))))
