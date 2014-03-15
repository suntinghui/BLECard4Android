package com.ble.util;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
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
