package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

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
	
	public static byte[] sign(byte[] encryptedMessage, PrivateKey prvKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		    Signature sig = Signature.getInstance("SHA1withRSA");
		    sig.initSign(prvKey);
		    sig.update(encryptedMessage);
		    return sig.sign();
		  }
	
	public static boolean verify(byte[] encryptedMessage , PublicKey pubKey, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		    Signature sig = Signature.getInstance("SHA1withRSA");
		    sig.initVerify(pubKey);
		   	sig.update(encryptedMessage);
		    return sig.verify(signature);
		  }
	
	

}
