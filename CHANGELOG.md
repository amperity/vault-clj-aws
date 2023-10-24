Change Log
==========

All notable changes to this project will be documented in this file.
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

* Project artifacts are now published as `com.amperity/vault-clj-aws`.
* The project now depends on `com.amperity/vault-clj-aws` 2.x which has
  completely new APIs; see that project for details.
* The login method now uses the AWS Java SDK's default credentials provider chain and
  default region provider chain for ease of use.
* The login method now supports all the extra parameters documented by Vault
  <https://developer.hashicorp.com/vault/api-docs/auth/aws#login>. The
  `iam_*` parameters are still auto-generated for ease of use.
* Removed unused dependencies.

## [0.0.4] - 2022-06-02

### Changed
- Updated dependency versions.


[Unreleased]: https://github.com/amperity/vault-clj/compare/0.0.4...HEAD
[0.0.4]: https://github.com/amperity/vault-clj/compare/0.0.2...0.0.4
[0.0.2]: https://github.com/amperity/vault-clj/releases/tag/0.0.2
