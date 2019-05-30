# Spring Security 5 Login with API Example

This work-in-progress prototype example uses Spring Boot 2.1.4 and Spring Security 5 to test and demonstrate OAuth2 Login and calling an API, as well as using `offline_access` to demonstrate using the refresh token.

## Setup

1. Create an Auth0 Web Application
2. Create an Auth0 API

Copy `webapp/src/main/resources/application.yml.example`:

```bash
cp webapp/src/main/resources/application.yml.example webapp/src/main/resources/application.yml
```

Update `webapp/src/main/resources/application.yml` with values for your domain, client ID, client secret (for your Web app), and API audience (for your API).

Copy `api/src/main/resources/application.yml.example`:

```bash
cp api/src/main/resources/application.yml.example api/src/main/resources/application.yml
```

Update `api/src/main/resources/application.yml` with values for your API audience and domain.

## Running

Start the web app:

```bash
cd webapp
./mvnw spring-boot:run
```

Start the API:

```bash
cd api
./mvnw spring-boot:run
```

Open your browser to [http://localhost:3000](http://localhost:3000).
You will be prompted to login, after which the user information from the ID token of the authenticated user will be displayed.

Click the "See expenses" link to demonstrate calling an API.

To test refresh token handling, update your API's settings to set a very low token expiration time (e.g., 10 seconds).