package co.hotwax.auth

import org.apache.commons.codec.binary.Base64
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset
import java.security.MessageDigest

public class PosixCredentialsMatcher  implements CredentialsMatcher {
    protected final static Logger logger = LoggerFactory.getLogger(OfbizShiroRealm.class)
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        // Assuming token and info contain the necessary data for comparison
        String crypted = (String) info.getCredentials();
        String password = (String) token.getCredentials();

        // Extract hashType, salt, and hashed password from crypted string
        int typeEnd = crypted.indexOf('$', 1);
        int saltEnd = crypted.indexOf('$', typeEnd + 1);
        String hashType = crypted.substring(1, typeEnd);
        String salt = crypted.substring(typeEnd + 1, saltEnd);
        String hashed = crypted.substring(saltEnd + 1);

        // Compute hash for the given password and compare with hashed password
        try {
            String computedHash = computeHash(hashType, salt, password.getBytes(Charset.forName("UTF-8")));
            return hashed.equals(computedHash);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false
    }

    private String computeHash(String hashType, String salt, byte[] bytes) throws  Exception {
        MessageDigest messagedigest = MessageDigest.getInstance(hashType);
        messagedigest.update(salt.getBytes(Charset.forName("UTF-8")));
        messagedigest.update(bytes);
        byte[] hashBytes = messagedigest.digest();
        return Base64.encodeBase64URLSafeString(hashBytes).replace('+', '.');
    }
}