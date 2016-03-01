package utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
	
	public static byte[] hash(String msg, byte[] salt) throws NoSuchAlgorithmException, IOException{
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(msg.getBytes());
		md.update(salt);
		byte[] serializedMsg = CryptoUtils.serialize(msg);
		byte[] hash = md.digest(serializedMsg);
		
		return hash;

		
	}
	
	public static String bytes2String(byte[] hash){
		return new BigInteger(hash).toString();
	}

}
