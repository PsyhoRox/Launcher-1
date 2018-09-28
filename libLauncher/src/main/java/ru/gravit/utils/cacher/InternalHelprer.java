package ru.gravit.utils.cacher;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.utils.helper.SecurityHelper.DigestAlgorithm;

class InternalHelper {
	public static String md5(String str) {
		// Create MessageDigest instance for MD5
		MessageDigest md = SecurityHelper.newDigest(DigestAlgorithm.MD5);
		// Add bytes to digest
		md.update(str.getBytes(IOHelper.UNICODE_CHARSET));
		return stringDigest(md);
	}

	public static String md5(byte[] in) {
		// Create MessageDigest instance for MD5
		MessageDigest md = SecurityHelper.newDigest(DigestAlgorithm.MD5);
		// Add bytes to digest
		md.update(in);
		return stringDigest(md);
	}

	public static String md5(InputStream in) throws IOException {
		// Create buffer
		byte[] buffer = IOHelper.newBuffer();
		// Create MessageDigest instance for MD5
		MessageDigest md = SecurityHelper.newDigest(DigestAlgorithm.MD5);
		// Add bytes to digest
		for (int length = in.read(buffer); length != -1; length = in.read(buffer))
			md.update(buffer, 0, length);
		return stringDigest(md);

	}

	private static String stringDigest(MessageDigest md) {
		// Get the hash's bytes
		byte[] bytes = md.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(md5("naf"));
	}
}
