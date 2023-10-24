Change Log
==========

All notable changes to this project will be documented in this file.
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

* Project artifacts are now published as `com.amperity/vault-clj-aws`.
* The project now depends on and integrates with
  [com.amperity/vault-clj](https://github.com/amperity/vault-clj) 2.x. This has
  completely new APIs compared to the old `amperity/vault-clj` 1.x and earlier.
  See the vault-clj repo for details.
* The login method now uses the AWS Java SDK's default credentials provider chain and
  default region provider chain.
* The `login` method now supports all the extra parameters documented by Vault
  <https://developer.hashicorp.com/vault/api-docs/auth/aws#login>, as keywords. Notably,
  `:role` can be passed as a parameter to the `login` method to allow a single
  AWS principal to log in to different Vault roles. The `iam_*` parameters are
  still auto-generated for ease of use.
* Removed unused dependencies.

## [0.0.4] - 2022-06-02

### Changed
- Updated dependency versions.


[Unreleased]: https://github.com/amperity/vault-clj/compare/0.0.4...HEAD
[0.0.4]: https://github.com/amperity/vault-clj/compare/0.0.2...0.0.4
[0.0.2]: https://github.com/amperity/vault-clj/releases/tag/0.0.2
