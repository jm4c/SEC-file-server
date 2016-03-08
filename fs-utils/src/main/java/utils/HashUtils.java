package utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static byte[] hash(String msg, byte[] salt) throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        
        if (salt != null) {
            md.update(salt);
        }

        byte[] serializedMsg = CryptoUtils.serialize(msg);
        byte[] hash = md.digest(serializedMsg);

        return hash;
    }
    
    public static byte[] hash(byte[] data, byte[] salt) throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        
        if (salt != null) {
            md.update(salt);
        }

        byte[] serializedMsg = CryptoUtils.serialize(data);
        byte[] hash = md.digest(serializedMsg);

        return hash;
    }

}
