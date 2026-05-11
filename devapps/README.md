# Devapps

This is devapps folder contains all running examples integration in UAA.

1. **deployment**: Contains resources and Docker Compose configuration for running the project locally.
2. **uaa-example-app**: Example application server-side allows you to run with different profiles. See [uaa-example-app](./uaa-example-app/README.md) to start server.
3. **uaa-example-app-client**: Example client-side application demonstrating how to authenticate using different authentication protocols. See [uaa-example-app-client](./uaa-example-app-client/README.md) to start client.
4. **uaa-example-helm**: Contains Helm charts and resources for deploying the example application to Kubernetes. See more [uaa-example-helm](./uaa-example-helm/README.md)
5. **uaa-example-rest-client**: Example application that uses a REST client to connect to `uaa-example-app` with `certificate-based authentication`.
6. **uaa-example-server-side-rendering**: Example to start different server then demonstrate communication between java servers using OIDC/Oauth2 protocol.

Note: Instead of duplicating the configuration properties consider to use multiple profiles.

### UAA example app (devapps) components
- Frontend: UAA-example-app-client
- Backend: UAA-example-app
- Keycloak
- Active directory: samba4
- Database: H2
- Server side rendering