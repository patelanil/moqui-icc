# maarg-util

### Linking OFBiz and Moqui Instances
To properly link OFBiz and Moqui instances, the following configurations must be applied during deployment:

### Moqui Configuration
Add the following properties to the Moqui configuration file:

```
    <default-property name="ofbiz.instance.url" value="https://localhost:8443"/>
    <default-property name="ofbiz.instance.name" value="localhost"/>
```    
### OFBiz Configuration
Update the start.properties file located at framework/start/src/main/java/org/apache/ofbiz/base/start/start.properties with:

```
    maarg.instance.url=https://localhost:8443
```
### Moqui Authentication Using OFBiz UserLogin Model
To enable Moqui authentication using the OFBiz UserLogin model:
- Copy the shiro.ini file from maarg-util to the OFBiz framework using the following command:

    ```
    cp runtime/component/maarg-util/shiro.ini framework/src/main/resources/
    ```

- The OfbizShiroRealm.groovy class has been implemented to authenticate users via the OFBiz UserLogin model and create/update the respective user account in the Moqui data model.

# Developer Setup for OMS Configuration

## Overview
To streamline the setup process, an `omsSetup` Gradle task has been added. This task configures the **JWT** and **OMS Encryption** flows.

## Steps to Configure

1. **Run the `omsSetup` Gradle Task**:
  - Execute the following command in your project root directory:
    ```bash
    ./gradlew omsSetup
    ```
  - This task will:
    - Configure the JWT settings.
    - Enable the OMS encryption flow.

2. **Verify Configuration**:
  - Check that the JWT and OMS encryption configurations have been correctly applied:
    - JWT-related files and settings should be in place.
    - OMS encryption dependencies and keys should be set up.

3. **Restart the Application**:
  - Restart your application to apply the configuration changes.

## Benefits of the `omsSetup` Task
- Automates the setup process for developers.
- Ensures consistent configuration across environments.
- Reduces the manual effort required to configure JWT and OMS encryption flows.


