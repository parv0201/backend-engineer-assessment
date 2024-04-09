## Introduction
This project adds

## Setup

### Pre-requisities

To run the application you would require:

- [Java](https://www.azul.com/downloads/#zulu)
- [Stripe API Keys](https://stripe.com/docs/keys)

### On macOS:

First, you need to install Java 21 or later. You can download it from [Azul](https://www.azul.com/downloads/#zulu) or
use [SDKMAN](https://sdkman.io/).

```sh
brew install --cask zulu21
```

You can install Docker using Homebrew

```sh
brew install docker
```

or visit [Docker Installation](https://docs.docker.com/get-docker/) for more information.

### On Windows:

First, you need to install Java 21 or later. You can download it from [Azul](https://www.azul.com/downloads/?version=java-21-lts&os=windows&package=jdk#zulu)

You can install Docker using [Docker Desktop](https://docs.docker.com/desktop/install/windows-install/)

### Other platforms

Please check the official documentation for the installation of Java, Temporal, and Docker for your platform.

### Stripe API Keys

Sign up for a Stripe account and get your API keys from the [Stripe Dashboard](https://dashboard.stripe.com/apikeys).
Then in `application.properties` file add the following line with your secret key.

```properties
stripe.api-key=sk_test_51J3j
```

## Run
You can run the application using the following command or using your IDE.

```sh
./gradlew bootRun
```
You can also use below command if you want to use docker compose. It uses [dockerfile](Dockerfile) for running the application container 

```sh
docker compose -f "compose-prod.yaml" up
```

### Other commands

#### Lint
To run lint checks, use the following command

```sh
./gradlew sonarlintMain
```

#### Code Formatting
To format the code, use the following command

```sh
./gradlew spotlessApply
```

#### Tests
To execute unit and integration tests, use following command.
Please add the stripe api key in `application.properties` file before running tests since integration tests interact with Stripe

```sh
./gradlew test
```


### Docker Compose support

This project contains a Docker Compose file named [compose.yaml](compose.yaml). It also have separate [compose-prod.yaml](compose-prod.yaml) file which can be used for running application container via docker compose.
In this file, the following services have been defined:

- postgres: [`postgres:latest`](https://hub.docker.com/_/postgres)
- temporal: [`temporalio/auto-setup:1.22.7`](https://hub.docker.com/r/temporalio/auto-setup)

### Implementation
#### Assumptions

- User can create only one account for an emailId. Multiple accounts with same email should not be allowed
- Any errors arising from temporal workflow are not handled gracefully.

#### Details

- Implemented separate workflow for create account and update account. This can help in adding any more steps if required in separate workflow
- Added `providerType` in the request body of POST /accounts API. Based on this parameter, system can choose the correct payment provider SDK / API
- Created a factory `PaymentProviderFactory` class to create correct payment provider implementation object based on `providerType` received in the request
- Added two new columns to accounts table - `provider_id` & `provider_type` to store customer id returned by payment provider and payment provider type respectively
- Added a new PATCH /accounts/{accountId} API which can update account details in database as well as to the payment provider
- Added necessary Workflow and Activity implementations required for temporal workflow
- Created a multi-stage Dockerfile which can build docker image of the application
- Leveraged Spring profiles for customizing database url and temporal url for production use cases

#### Area of improvement

- Current implementation does not handle temporal errors gracefully (temporal server being down, any unexpected errors). This can be improved to make the application fault-tolerant
- Metrics can be exposed to measure performance of the application.

### Code walk through Video
https://youtu.be/wPtc0YrUJl8
