#  Oauth2 client for server side rendering

The server is confifgured with auth-type OAUTH and OAUTH2_CLIENT that will secure the `IndexController` access.

To have a right to access the api(s) of `IndexController`, firstly, it's required to login via the endpoint `http://localhost:9092/oauth2/authorization/uaa-auth-client`. After successful login, the server will interact with client browser by using cookie. In other words, the session is maintained by the server.

## Start the application

    gradle bootRun

No additional profile is required.

## Access api(s)
1. Enter the endpoint `http://localhost:9092/oauth2/authorization/uaa-auth-client` and make the login.

2. After successful login, it will redirect to endpoint `http://localhost:9092` with the content `Welcome to Server Side Rendering (Oauth2 client mode)` (before login, it would be the 401 page).

3. Access to the api `http://localhost:9092/callDevAppExampleEndPoint`, the list of companies is returned.

> **_NOTE:_** make sure the `uaa-example-app` is running and the endpoint to load companies is confugured via the property `mgmtp.a12.uaa.example.dev.app.url`.