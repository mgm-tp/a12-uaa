<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# User Authentication Authorization

User Authentication Authorization (UAA) is a library for handling the security aspects of your application. It can be used as a standalone library or within
a Spring Boot application.

This repository contains:
- Frontend authentication client package for JavaScript/TypeScript applications
- Spring Boot extension for authentication principal integration
- Example applications under `devapps/` for local development and testing

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.  
Please check the [LICENSE](./LICENSE) file for details.

---

## Getting Started

### How to Use It

#### Import & Install

For the client, install the latest npm package with:

```sh
npm install @com.mgmtp.a12.uaa/uaa-authentication-client
```

For the server:

**Gradle:**

```gradle
dependencies {
    implementation 'com.mgmtp.a12.uaa:uaa-authentication-principal-extension-spring-boot-autoconfigure:<version>'
}
```

**Maven:**

```xml

<dependency>
    <groupId>com.mgmtp.a12.uaa</groupId>
    <artifactId>uaa-authentication-principal-extension-spring-boot-autoconfigure</artifactId>
    <version>${version}</version>
</dependency>
```

### How to Build and Run

#### Prerequisites (tools and their versions)

| Tool                              | Version  |
|-----------------------------------|----------|
| [JDK](https://openjdk.org/)       | `21`     |
| [Gradle](https://gradle.org/)     | `8.14.x` |
| [Node](https://nodejs.org/)       | `22.x`   |
| [Npm](https://www.npmjs.com/)     | `10.9.x` |

#### How to Build

To build the project, follow the steps below.

For the client, install all dependencies and compile the package at the root project:

```shell
npm install
npm run compile
```

For the server (using the Gradle Wrapper), run the following command from the project root:

```shell
./gradlew assemble
```

#### How to Test

To run the tests, use the following commands.

For the client, run the unit tests from the project root:

```shell
npm run test
```

For the server (using the Gradle Wrapper), run the following command from the project root:

```shell
./gradlew check
```

#### How to Run

To run the client, first install and compile from the project root, then change to the devapps/uaa-example-app-client directory:

```shell
cd devapps/uaa-example-app-client
```

* Start the client:

```shell
npm start
```

For the server, using the Gradle Wrapper, run the following command from the project root with the local authentication type:

```shell
./gradlew startExampleServer -PPROFILES=uaa_local
```

If you want to run the server with other authentication types, you can choose one of the following profiles:

- uaa_ldap: For login with LDAP authentication
- uaa_apikey: For login with API Key authentication
- uaa_certificate: For login with Certificate authentication
- uaa_oauth2_jwt: For login with OAuth2 authentication
- uaa_saml: For login with SAML authentication
- uaa_all_types: For login with all authentication types

For setting up Keycloak or an LDAP server, please refer to the [documentation](https://geta12.com/#/docs/2025.06/ext2/uaa/uaa-documentation-src).

#### How to Access It

Once running, open your browser and navigate to:

- **UAA-Example-Client**: <http://localhost:3000>
- **UAA-Example-Server**: <http://localhost:8080>

For local development only, you can log in using the username and password `admin/admin` using the first login button.

---

### Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)