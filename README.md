vault-clj-aws
=============

An extension to vault-clj to support authenticating the Vault client
using the [AWS auth method](https://developer.hashicorp.com/vault/api-docs/auth/aws).

This is a separate artifact to avoid bulking up the core project and its dependencies.

The most common use case for the AWS auth method is to get AWS credentials into
Clojure code running on a "bare metal" EC2 instance. Usually only if that
Clojure code is the only significant application running on that EC2 instance.

## Example Usage

```clojure
;; require needed namespaces
(require '[vault.client.http :as http]
         '[vault.auth.ext.aws :as aws])

(def vault (http/http-client "https://localhost:8200"))

(aws/login vault {:role "my-vault-auth-role"})
```

### Testing

It takes a fair amount of setup and configuration to test the AWS auth method.
You'll need a Vault server, the Vault AWS auth method enabled and configured
with roles, and a set of AWS credentials belonging to an IAM user or IAM role.

A common use case for AWS auth is to get AWS credentials into running on an EC2 instance.
If you happen to have such a setup already, then the "last mile" can be tested somewhat easily:

SSH into a running EC2 instance and grab credentials from from the instance metadata
service (IMDS). The profile in this example is `srv-instance-example`:

```bash
$ ssh ip-12-34-56-78.us-west-2.compute.internal
ubuntu@ip-12-34-56-78.us-west-2.compute.internal $ curl -s http://169.254.169.254/latest/meta-data/iam/security-credentials/srv-instance-example
{
  "Code" : "Success",
  "LastUpdated" : "2019-06-14T04:28:36Z",
  "Type" : "AWS-HMAC",
  "AccessKeyId" : "AAABBB",
  "SecretAccessKey": "longsecret",
  "Token": "verylongsessiontoken",
  "Expiration": "2019-06-14T10:36:49Z"
}
```

Copy out these credential from `AccessKeyId`, `SecretAccessKey`, and `Token`.
These can be used for local testing from a `lein repl`.

```
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export AWS_SESSION_TOKEN=...
lein repl
```
