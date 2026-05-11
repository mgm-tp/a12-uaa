# Example Application Client

This is a demo client application that showcases how to use the UAA Authentication Client Library to perform login with multiple authentication types: Local, LDAP, Oauth2 and Saml.

## How to run Example Application Client:

1. Configure Authentication Settings
   Locate the file: uaa-example-app-client/public/uaaConfiguration.js
   * `__idpHost__` - Replace with Keycloak Host URL (e.g. `http://localhost:9090`).
   * `__idpRealm__` - Replace with your realm (e.g. `UAARealm`).
   * `__idpClientId__` - Replace with Client Id (e.g. `uaa-spa-client`).
   * `__serverURL__` - Replace with Client Id (e.g. `http://localhost:8080`).

2. Build the Authentication Library

   Navigate to the UAA Authentication Client package and build it:
   ```shell script
   cd ../../uaa-authentication-client
   npm install
   npm run dist:client
   npm run compile
   ```

3. Set Up the Example Client
   Return to the example client directory and Install dependencies:
    ```shell script
    cd devapps/uaa-example-app-client
    npm install
    ```
4. Run the Example Application Client
    ```shell script
    npm start
    ```

   The application started in port 3000.