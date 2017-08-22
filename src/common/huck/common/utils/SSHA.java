package huck.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SSHA {
	private static final int SALT_LEN = 8;
	public static String createDigest(byte[] salt, String entity) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA");
			sha.reset();
			sha.update(entity.getBytes("UTF-8"));
			sha.update(salt);
			byte[] hash = sha.digest();
			byte[] result = new byte[hash.length + salt.length];
			System.arraycopy(hash, 0, result, 0, hash.length);
			System.arraycopy(salt, 0, result, hash.length, salt.length);
			return BASE64.encode(result);
		} catch( UnsupportedEncodingException ex ) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static byte[] getSalt(String hashString) {
		byte[] bytes = BASE64.decode(hashString);
		byte[] salt = new byte[SALT_LEN];
		System.arraycopy(bytes, bytes.length-SALT_LEN, salt, 0, SALT_LEN);
		return salt;
	}

	public static String createDigest(String entity) {
		return createDigest(randSalt(),entity);
	}
	
	public static byte[] randSalt(){
		byte[] b = new byte[SALT_LEN];
		for(int i = 0;i<SALT_LEN;i++){
			byte bt = (byte)(((Math.random())*256)-128);
			b[i]=bt;
		}
		return b;
	}
}