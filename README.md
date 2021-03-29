Just checking
# Cleaner service

## Building

To compile and run tests, run:

```
mvn clean install
```

To start the app in dev-mode (with hot-code reload!), run:

```
mvn clean install quarkus:dev
```


## Running the application

The JAR produced for running is a fat jar to more easily grab the jar and all
its dependencies inside a Dockerfile.

To run the application, first build it, then run:

```
java -jar target/cleaner-runner.jar
```


### Application Information

The app version, git revision, and build time are both printed on the app logs
at startup as well as available on the REST endpoint `/version`


### Monitoring

We use Microprofile metrics to monitor our application. The metrics are exposed
using Prometheus format on the `/metrics` endpoint.

For more information see: https://quarkus.io/guides/metrics-guide
