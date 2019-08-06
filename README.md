vault-clj-aws
=============

An extension to vault-clj to support authentication to the
[aws auth backend ](https://www.vaultproject.io/api/auth/aws/)
without bulking up the core project and its dependencies.


## Using

Since the auth backend is configured for iam authentication any iam principal
can be used. In practice this might be most useful via a configured Instance Profile
where an EC2 instance metadata credential is used to authenticate to STS.

### Setup

TODO: describe vault aws auth backend, role configuration

### Testing

Find the credentials from a running EC2 instance from the internal metadata endpoint. The profile in this example is `srv-instance-example`
```bash
% ssh ip-12-34-56-78.us-west-2.compute.internal
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

Use these credentials to perform testing from a local laptop.
```clojure
user=> (require '[vault.client.ext.aws :as aws])
user=> (require '[vault.client.http :as client])
user=> (require '[vault.core :as vault])

user=> (def c (vault/new-client "https://www.vault.systems:8200"))
user=> (def creds (aws/credentials "AAABBB" "longsecret" "verylongsessiontoken"))
user=> (client/authenticate-type! c :aws-iam {:iam-role "srv-instance-example" :test-credentials creds})
Jun 13, 2019 9:45:54 PM vault.client.http invoke
INFO: Successfully authenticated to Vault as AWS IAM Role srv-instance-example for policies: default, instance-example.local
{:client-token "token", :accessor "token-acessor", :policies ["default" "instance-example.local"], :token-policies ["default" "instance-example.local"], :metadata {:account-id "123456789012", :auth-type "iam", :canonical-arn "arn:aws:iam::123456789012:role/srv-instance-example", :client-arn "arn:aws:sts::123456789012:assumed-role/srv-instance-example/i-0e709afbc21e70c0f", :client-user-id "AAABBB", :inferred-aws-region "", :inferred-entity-id "", :inferred-entity-type ""}, :lease-duration 60, :renewable true, :entity-id "7b43fca2-17f6-49fc-967f-6e4e3d16a8ae", :vault.lease/expiry #object[java.time.Instant 0x374eea9c "2019-06-14T04:46:54.985Z"]}
```
