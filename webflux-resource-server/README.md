# Spring Security 5 Reactive Resource Server Prototype

This work-in-progress prototype example uses Spring Boot 2.1.4 and Spring Security 5 to test and demonstrate OAuth2 Resource Server support.

## Running

Copy `src/main/resources/application.yml.example`:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Update `application.yml` with values for your API audience and domain.

Run the application:

```bash
./mvnw spring-boot:run
```

Using a token for your API (can be obtained via the Test tab on the management dashboard), call your API:

Public API (no authorization required):

```bash
curl http://localhost:3000/api/public
```

Private API (authorization required):

```bash
curl http://localhost:3000/api/private/ \
    --header "Authorization: Bearer {ACCESS-TOKEN}
```

Private scoped API (authorization with `read:messages` scope required):

> Be sure to add the `read:messages` scope to your API and enable it for your API under Machine-to-Machine settings.

```bash
curl http://localhost:3000/api/private-scoped/ \
    --header "Authorization: Bearer {ACCESS-TOKEN}
```

## Open questions and TODO

- Is it following spec (enough)? Proper token validation, etc? Need to audit/verify.
- Only RS256 supported?
- Clock skew supported? [GH issue, maybe fixed?](https://github.com/spring-projects/spring-security/issues/5839)

