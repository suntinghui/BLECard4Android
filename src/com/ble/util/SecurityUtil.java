package com.ble.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {

	public static byte[] TripleDESEncry(byte[] keyByte, byte[] textByte) {
		try {
			SecretKey keySpec = new SecretKeySpec(keyByte, "DESede");
			Cipher e_cipher = Cipher.getInstance("DESede/ECB/NoPadding", "BC");
			e_cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] cipherText = e_cipher.doFinal(textByte);

			return cipherText;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] DESEncry(byte[] keyByte, byte[] textByte) {
		try {
			SecretKey keySpec = new SecretKeySpec(keyByte, "DES");
			Cipher e_cipher = Cipher.getInstance("DES/ECB/NoPadding", "BC");
			e_cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] cipherText = e_cipher.doFinal(textByte);

			return cipherText;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] DESDencry(byte[] keyByte, byte[] textByte) {
		try {
			SecretKey keySpec = new SecretKeySpec(keyByte, "DES");
			Cipher e_cipher = Cipher.getInstance("DES/ECB/NoPadding", "BC");
			e_cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] cipherText = e_cipher.doFinal(textByte);

			return cipherText;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String MD5Crypto(String str) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(str.getBytes());
			byte messageDigest[] = digest.digest();
			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String SHA1Crypto(String str) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(str.getBytes());
			byte messageDigest[] = digest.digest();
			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String xorHex(String input, String key) {
		StringBuffer sb = new StringBuffer(input);
		while (sb.length() % 16 != 0) {
			sb.append("00");
		}

		char[] chars = new char[sb.length()];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = toHex(fromHex(sb.charAt(i)) ^ fromHex(key.charAt(i)));
		}
		return new String(chars);
	}

	private static int fromHex(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		}
		if (c >= 'a' && c <= 'f') {
			return c - 'a' + 10;
		}
		throw new IllegalArgumentException();
	}

	private static char toHex(int nybble) {
		if (nybble < 0 || nybble > 15) {
			throw new IllegalArgumentException();
		}
		return "0123456789ABCDEF".charAt(nybble);
	}

	private static String toHexString(byte[] b) {
		char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}

}
