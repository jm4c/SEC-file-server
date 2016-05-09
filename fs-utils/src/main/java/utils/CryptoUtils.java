package utils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;

public class CryptoUtils {

    public static KeyPair setKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        byte[] outputBytes = out.toByteArray();
        out.close();
        return outputBytes;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Object outputObject = is.readObject();
        in.close();
        return outputObject;
    }

    public static byte[] sign(byte[] unsignedData, PrivateKey prvKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(prvKey);
        sig.update(unsignedData);
        return sig.sign();
    }

    public static boolean verify(byte[] signedData, PublicKey pubKey, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(pubKey);
        sig.update(signedData);
        return sig.verify(signature);
    }

    public static byte[] getHMACdigest(byte[] message, SecretKey secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = "HmacSHA1";
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(keySpec);
        return mac.doFinal(message);
    }



}
