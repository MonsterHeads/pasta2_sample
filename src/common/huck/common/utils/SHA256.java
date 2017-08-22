package huck.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA256 {
	public static void main(String... args) throws Exception {
		System.out.println(SHA256.createDigest(278180334199297l, MD5.createDigest("gorkWodWod@1")));
	}
	public static String createDigest(long salt, String entity) {
		byte[] saltBytes = new byte[8];
		saltBytes[0] = (byte)((salt>>>0)&0xFF);
		saltBytes[1] = (byte)((salt>>>8)&0xFF);
		saltBytes[2] = (byte)((salt>>>16)&0xFF);
		saltBytes[3] = (byte)((salt>>>24)&0xFF);
		saltBytes[4] = (byte)((salt>>>32)&0xFF);
		saltBytes[5] = (byte)((salt>>>40)&0xFF);
		saltBytes[6] = (byte)((salt>>>48)&0xFF);
		saltBytes[7] = (byte)((salt>>>56)&0xFF);
		return createDigest(saltBytes, entity);
	}
	public static String createDigest(byte[] salt, String entity) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			sha.reset();
			sha.update(entity.getBytes("UTF-8"));
			sha.update(salt);
			byte[] hash = sha.digest();
			return BASE32.encode(hash);
		} catch( UnsupportedEncodingException ex ) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

}