Change Log
==========

All notable changes to this project will be documented in this file.
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

- The library was essentially rewritten. Check README.md for updated usage info.
  Most importantly, this library is now based on `com.amperity/vault-clj` 2.x.
  Additionally:
    - For consistency with other Amperity open-source, the artifact has been renamed to `com.amperity/vault-clj-aws`
      and the default branch changed to `main`.
    - Reasonable defaults: the login method now uses the AWS Java SDK's default
      credentials and default region if none are provided.
    - The legacy AWS Java SDK v1 has been replaced with AWS Java SDK v2.
    - The dependency on `cheshire` / `jackson` for JSON has been removed in favor of `org.clojure/data.json`.
    - The vestigial dependency on `amperity/envoy` has been removed.

## [0.0.4] - 2022-06-02

### Changed
- Updated dependency versions.


[Unreleased]: https://github.com/amperity/vault-clj/compare/0.0.4...HEAD
[0.0.4]: https://github.com/amperity/vault-clj/compare/0.0.2...0.0.4
[0.0.2]: https://github.com/amperity/vault-clj/releases/tag/0.0.2
