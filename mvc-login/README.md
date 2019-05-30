# Spring Security 5 OIDC Login Prototype

This work-in-progress prototype example uses Spring Boot 2.1.4 and Spring Security 5 to test and demonstrate OAuth2 Login support.

## Running

Copy `src/main/resources/application.yml.example`:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Update `application.yml` with values for your domain, client ID, and client secret.

Run the application:

```bash
./mvnw spring-boot:run
```

Open your browser to [http://localhost:3000](http://localhost:3000).
You will be prompted to login, after which the user information from the ID token of the authenticated user will be displayed.

## Details

- Uses Spring Security 5 OAuth2/OIDC features
- Prototypes proper logout flow
- Uses Authorization Code grant

## Open questions and TODO

- Can implicit flow be supported?
- [Does not appear]((https://github.com/spring-projects/spring-security/issues/4442)) to support nonce.
- If nonce isn't supported, then by the spec implicit flow is not supported.
- Is it following spec (enough)? Proper token validation, etc? Need to audit/verify.
- Only RS256 supported?
- Clock skew supported? [GH issue, maybe fixed?](https://github.com/spring-projects/spring-security/issues/5839)
- Current required config is verbose

