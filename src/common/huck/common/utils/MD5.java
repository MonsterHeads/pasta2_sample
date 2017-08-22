package huck.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5 {
	public static String createDigest(String entity) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(entity.getBytes("UTF-8"));
			byte[] hash = md5.digest();
			StringBuffer buf = new StringBuffer(hash.length*2);
			for( int i=0; i<hash.length; i++ ) {
				buf.append(String.format("%02x", hash[i]));
			}
			return buf.toString();
		} catch( UnsupportedEncodingException ex ) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}