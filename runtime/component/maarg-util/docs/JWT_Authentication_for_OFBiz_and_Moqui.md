# JWT Authentication for OFBiz and Moqui

## Overview

Both OFBiz and Moqui can be authenticated using the same JWT (JSON Web Token). This JWT token is generated using a secret key and an issuer, which are configured at the time of deployment. The same token is validated by both instances to ensure secure and authenticated requests.

## Token Generation

To generate the JWT token, a common shared secret key and an issuer (configured during deployment) are used. The process of generating and validating the JWT token ensures that both OFBiz and Moqui can authenticate and authorize requests consistently.

### Steps to Generate JWT Token

1. **Configure Secret Key and Issuer:**
    - During deployment, configure a shared secret key and issuer for both OFBiz and Moqui instances.
    - The issuer should be the instance name (ofbiz.instance.name) configured during deployment.
    - Set the token expiration time. The default expiration time is 300 seconds.

2. **Generate JWT Token:**
    - Use the configured secret key and issuer to generate the JWT token.
    - The token will include claims such as `userLoginId`

### Token Validation

To validate the JWT token, both OFBiz and Moqui will use the same shared secret key and issuer. The process involves checking the token's claims and ensuring that the userAccount.userName (in moqui) and UserLogin.userLoginId (in ofbiz) match in both instances.

### Steps to Validate JWT Token
1. **Configure Secret Key and Issuer:**
    - Ensure both OFBiz and Moqui have the same secret key and issuer configured.

2. **Validate JWT Token:**
    - Decode the JWT token and verify its claims using the shared secret key and issuer.
    - Check that the userAccount.userName and UserLogin.userLoginId are the same in both instances.

## Important Considerations

1. **User Consistency:** 
   - Ensure that userAccount.userName and UserLogin.userLoginId are the same in both OFBiz and Moqui instances. If they do not match, JWT authentication will fail as the username will not be found.

2. **Secure Storage:** 
   - Store the secret key securely and avoid exposing it in logs or version control systems. The default token expiration time is 300 seconds, but this can be configured
3. **Token Expiry:** 
   - Implement token expiry and refresh mechanisms to enhance security.The default token expiration time is 300 seconds, but this can be configured
   
     `<default-property name="jwt.default.expireTime" value="300"/>`

### JWT Token Configuration
To configure JWT token authentication:
- Ensure the JWT secret key is the same for both OFBiz and Moqui instances.
- Copy the JwtManager.groovy file from maarg-util to the OFBiz framework location:
    ```
    mkdir -p framework/src/main/groovy/co/hotwax/auth/
    cp runtime/component/maarg-util/src/main/groovy/co/hotwax/auth/JWTManager.groovy framework/src/main/groovy/co/hotwax/auth/JWTManager.groovy
    ```
- Apply the JwtToken.patch located in maarg-util:

    ```
    patch -p0 < runtime/component/maarg-util/patches/JwtToken.patch`
    ```


By following these guidelines, you can ensure secure and consistent JWT authentication for both OFBiz and Moqui instances.
   

