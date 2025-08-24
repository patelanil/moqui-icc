# Moqui Authorization Using OFBiz Data Model

## Overview

This component integrates Moqui with the OFBiz data model for user authentication. Moqui uses Apache Shiro for its authorization implementation. In this component, the `OfbizShiroRealm` class is added to authenticate users using the OFBiz data model. Additionally, the `PosixCredentialsMatcher` class is implemented to handle password encryption using the Posix method, similar to OFBiz.

## Key Components

- **OfbizShiroRealm**: This class uses the OFBiz data model to authenticate users.
- **PosixCredentialsMatcher**: This class implements Shiro's `CredentialsMatcher` to provide token-based authentication using the Posix encryption method.

## Authentication Flow

1. **User Authentication**:
    - The user is authenticated using their OFBiz username and password stored in the `UserLogin` entity.
    - Passwords are encrypted using the Posix method.

2. **User Account Handling**:
    - Upon successful authentication, the `loginPostPassword` method checks for an existing `UserAccount` where `userName = userLogin.userLoginId`.
    - If an account is found, it returns the `UserAccount`.
    - If no account is found, a new `UserAccount` is created with `UserAccount.userName = userLogin.userLoginId`, and the password is stored using Moqui's encryption.

## Limitations

- The current implementation does not support OFBiz's user password reset or enable/disable functionalities. These actions should be performed from the OFBiz application.

## Configuration

### Shiro Configuration

1. **Shiro SecurityManager Realms**:
    - The `maarg-util/shiro.ini` file sets `shiro.securityManager.realms` to `co.hotwax.auth.OfbizShiroRealm`.

2. **Setup Instructions**:
    - To enable OFBiz authentication, place the `maarg-util/shiro.ini` file into `framework/src/main/resources/shiro.ini`.

### OFBiz Datasource Configuration

- Ensure the OFBiz datasource is properly configured.

### Required Write Access

- The following OFBiz entities require write access:
    - `org.apache.ofbiz.security.login.UserLogin`
    - `org.apache.ofbiz.security.login.UserLoginPasswordHistory`

## Setup Steps

1. **Clone the Repository**:
   ```sh
   git clone <repository-url>
   
2. **Place shiro.ini File**:
   Copy the maarg-util/shiro.ini file to the following location:
   ```sh
   cp maarg-util/shiro.ini framework/src/main/resources/shiro.ini

3. **Configure OFBiz Datasource**:
Ensure your OFBiz datasource is correctly configured to connect to your OFBiz database.

4. **Grant Write Access**:
Ensure that the application has write access to the required OFBiz entities (UserLogin and UserLoginPasswordHistory).

## TODO
- Map user login security groups with Moqui security groups.



