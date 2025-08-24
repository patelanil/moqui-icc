# OmsEntityCrypto Utility Class

## Overview

The `OmsEntityCrypto` class is a utility designed for secure encryption and decryption of data, leveraging Apache Shiro's cryptographic capabilities. It facilitates secure key management, encryption, and decryption of sensitive data within Moqui-based applications.

---

## Key Features

### 1. **Core Functionalities**
- **Data Encryption**: Convert objects into encrypted strings.
- **Data Decryption**: Convert encrypted strings back into objects.

### 2. **Key Management**
- Automatically generate and store encryption keys in the `EntityKeyStore` entity.
- Cache keys in memory for improved performance.

### 3. **Flexible Encryption**
- Supports salted and unsalted encryption schemes.
- Uses AES encryption with configurable padding and operation modes.

### 4. **Performance Optimizations**
- Utilizes a `ConcurrentMap` for caching encryption keys to minimize database lookups.

---

## Design Components

### **1. OmsEntityCrypto Class**
- **Static Utility**: Provides thread-safe encryption and decryption methods.
- **Key Handling**:
    - Retrieves or creates encryption keys dynamically.
    - Stores keys securely in the `EntityKeyStore` entity.
- **Encryption/Decryption Methods**:
    - `encrypt`: Encrypts an object to a string.
    - `decrypt`: Decrypts a string to an object.

### **2. ShiroStorageHandler Inner Class**
Manages encryption and decryption operations using Apache Shiro.
- **Key Management**:
    - Generates, encodes, and decodes AES keys.
    - Optionally supports Key Encryption Key (KEK) for added security.
- **Encryption/Decryption**:
    - Supports salted and unsalted encryption schemes.
    - Utilizes Apache Shiro's `AesCipherService` for cryptographic operations.

### **3. EncryptMethod Enum**
Defines available encryption methods:
- `FALSE`: No encryption.
- `TRUE`: Standard encryption.
- `SALT`: Salted encryption.

---

## Usage Scenarios

### **1. Encrypting Data**
Call the `encrypt` method:
```java
String encryptedData = OmsEntityCrypto.encrypt(ec, "keyName", EncryptMethod.SALT, objectToEncrypt);
```
### **2. Encrypting Data**
Call the decrypt method:
```java
Object decryptedData = OmsEntityCrypto.decrypt(ec, "keyName", EncryptMethod.SALT, encryptedString);
```

### **3. Key Management**
Keys are automatically generated and stored in the EntityKeyStore entity if not found.


# Features in Detail

## Encryption Process

1. **Retrieve Key**:
    - Find or create an encryption key.
2. **Serialize Object**:
    - Convert the object into a byte array for encryption.
3. **Encrypt Data**:
    - Encrypt the byte array using the specified encryption method.

## Decryption Process

1. **Retrieve Key**:
    - Locate the encryption key used for encryption.
2. **Decrypt Data**:
    - Decrypt the string back into a byte array.
3. **Deserialize Object**:
    - Convert the decrypted byte array back into the original object.

---

## Limitations

1. **Key Management Dependency**:
    - Requires a fully functional `EntityKeyStore` entity for storing and retrieving keys.

2. **Serialization Constraints**:
    - Objects to be encrypted must implement the `Serializable` interface.

3. **Default Key Size**:
    - Uses a 128-bit AES key by default, which may not meet the requirements for high-security environments.

# Steps to Enable the OMS Encryption Flow

1. **Apply the Patch**:
    - Navigate to the root directory of your project.
    - Run the following command to apply the `EntityCrypto.patch`:
      ```bash
      patch -p0 < runtime/component/maarg-util/patches/EntityCrypto.patch
      ```

2. **Copy the `OmsEntityCrypto.groovy` File**:
    - Locate the `OmsEntityCrypto.groovy` file in the source directory:
      ```
      runtime/component/maarg-util/src/main/groovy/co/hotwax/util/OmsEntityCrypto.groovy
      ```
    - Copy it to the framework source directory:
      ```
      framework/src/main/groovy/co/hotwax/util/OmsEntityCrypto.groovy
      ```

3. **Restart the Application**:
    - After applying the patch and copying the required file, restart your application to ensure the changes are reflected.

