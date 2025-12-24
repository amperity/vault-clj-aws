vault-clj-aws
=============

An extension to [com.amperity/vault-clj](https://github.com/amperity/vault-clj)
that implements the [AWS auth method](https://www.vaultproject.io/api/auth/aws/).

This auth method is in a separate package because it includes a dependency
on the AWS Java SDK, which not all clients need or want.


## Usage

Add this library to project dependencies alongside the main vault-clj library.
For example, with lein:

```clojure
[com.amperity/vault-clj "2.3.588"]
[com.amperity/vault-clj-aws "2.0.0"]
```

Log in to Vault using AWS credentials:

```clojure
=> (require '[vault.auth.aws :as aws])

;; Basic form:
;; Uses the AWS SDK default credentials and default region,
;; and Vault role matching the name of the AWS credentials
=> (aws/login vault-client)

;; Full form:
=> (aws/login vault-client {:role "srv-example" :aws-region "us-west-2" :aws-credentials ,,,})
```

If the AWS auth method is mounted at a non-default location,
configure the mount point on the client using `with-mount`.
