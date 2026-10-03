Contributing
============
TODO: Add contributing info

CI configuration
----------------

Marathon Cloud instrumentation tests are paused by default. To enable them, set
the `ENABLE_MARATHON_TESTS` repository variable to `true` and configure the
`MARATHON_CLOUD_API_TOKEN` repository secret.

Coverage uploads authenticate through GitHub Actions OIDC and do not require a
`CODECOV_TOKEN` repository secret.
