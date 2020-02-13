/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple encoding utility functions.
 * @author Matthew Tropiano
 */
public final class EncodingUtils
{
	private EncodingUtils() {}

	/**
	 * Returns a hash of a set of bytes digested by an encryption algorithm.
	 * Can return null if this Java implementation cannot perform this.
	 * Do not use this if you care if the algorithm is provided or not.
	 * @param bytes the bytes to encode.
	 * @param algorithmName the name to the algorithm to use.
	 * @return the resultant byte digest, or null if the algorithm is not supported.
	 */
	public static byte[] digest(byte[] bytes, String algorithmName)
	{
		try {
			return MessageDigest.getInstance(algorithmName).digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	/**
	 * Returns a 20-byte SHA-1 hash of a set of bytes.
	 * Can return null if this Java implementation cannot perform this,
	 * but it shouldn't, since SHA-1 is mandatorily implemented for all implementations.
	 * @param bytes the input bytes.
	 * @return the resultant 20-byte digest.
	 * @see #digest(byte[], String)
	 */
	public static byte[] sha1(byte[] bytes)
	{
		return digest(bytes, "SHA-1");
	}

	/**
	 * Returns a 16-byte MD5 hash of a set of bytes.
	 * Can return null if this Java implementation cannot perform this,
	 * but it shouldn't, since MD5 is mandatorily implemented for all implementations.
	 * @param bytes the input bytes.
	 * @return the resultant 16-byte digest.
	 * @see #digest(byte[], String)
	 */
	public static byte[] md5(byte[] bytes)
	{
		return digest(bytes, "MD5");
	}

	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * Uses + and / as characters 62 and 63.
	 * @param in the input stream to read to convert to Base64.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(InputStream in) throws IOException
	{
		return asBase64(in, '+', '/');
	}

	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * @param in the input stream to read to convert to Base64.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(InputStream in, char sixtyTwo, char sixtyThree) throws IOException
	{
		final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		final char BLANK = '=';
		
		String alph = ALPHABET + sixtyTwo + sixtyThree;
		
		StringBuilder out = new StringBuilder();
		int octetBuffer = 0x00000000;
		int bidx = 0;
		
		byte[] buffer = new byte[16384];
		int buf = 0;
		
		while ((buf = in.read(buffer)) > 0) for (int i = 0; i < buf; i++)
		{
			byte b = buffer[i];
			
			octetBuffer |= ((b & 0x0ff) << ((2 - bidx) * 8));
			bidx++;
			if (bidx == 3)
			{
				out.append(alph.charAt((octetBuffer & (0x3f << 18)) >> 18));
				out.append(alph.charAt((octetBuffer & (0x3f << 12)) >> 12));
				out.append(alph.charAt((octetBuffer & (0x3f << 6)) >> 6));
				out.append(alph.charAt(octetBuffer & 0x3f));
				octetBuffer = 0x00000000;
				bidx = 0;
			}
		}
		
		if (bidx == 2)
		{
			out.append(alph.charAt((octetBuffer & (0x3f << 18)) >> 18));
			out.append(alph.charAt((octetBuffer & (0x3f << 12)) >> 12));
			out.append(alph.charAt((octetBuffer & (0x3f << 6)) >> 6));
			out.append(BLANK);
		}
		else if (bidx == 1)
		{
			out.append(alph.charAt((octetBuffer & (0x3f << 18)) >> 18));
			out.append(alph.charAt((octetBuffer & (0x3f << 12)) >> 12));
			out.append(BLANK);
			out.append(BLANK);
		}
		
		return out.toString();
	}

	/**
	 * Returns a new byte array, delta-encoded.<br>Use with deltaDecode() to decode.
	 * <p>After the first byte, the change in value from the last byte is stored.
	 * <br>For example, a byte sequence <code>[64,92,-23,33]</code> would be returned
	 * as: <code>[64,28,-115,56]</code>.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] deltaEncode(byte[] b)
	{
		byte[] delta = new byte[b.length];
		delta[0] = b[0];
		for (int i = 1; i < b.length; i++)
			delta[i] = (byte)(b[i] - b[i-1]);
		
		return delta;
	}

	/**
	 * Returns a new byte array, delta-decoded.<br>Decodes sequences made with deltaEncode().
	 * <p>After the first byte, the change in value from the last byte is used to get the original value.
	 * <br>For example, a byte sequence <code>[64,28,-115,56]</code> would be returned
	 * as: <code>[64,92,-23,33]</code>.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] deltaDecode(byte[] b)
	{
		byte[] delta = /*decompressBytes(b)*/ b;
		
		byte[] out = new byte[delta.length];
		out[0] = delta[0];
		for (int i = 1; i < b.length; i++)
			out[i] = (byte)(out[i-1] + delta[i]);
		
		return out;
	}

	/**
	 * Returns a new byte array, Carmacized.
	 * Named after John D. Carmack, this will compress a sequence
	 * of bytes known to be alike in contiguous sequences.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] carmacize(byte[] b)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int seq = 1;
		byte prev = b[0];
	
		for(int i = 1; i < b.length; i++)
		{
			if (b[i] == prev)
				seq++;
			else if (prev == -1 || seq > 3)
			{
				while (seq > 0)
				{
					bos.write(255);
					bos.write(prev);
					bos.write(seq > 255 ? 255 : seq);
					seq -= 255;
				}
				prev = b[i];
				seq = 1;
			}
			else
			{
				for (int x = 0; x < seq; x++)
					bos.write(prev);
				prev = b[i];
				seq = 1;
			}
		}
	
		if (seq > 3)
			while (seq > 0)
			{
				bos.write(255);	
				bos.write(prev);
				bos.write(seq > 255 ? 255 : seq);
				seq -= 255;
			}
		else
			for (int x = 0; x < seq; x++)
				bos.write(prev);
		
		return bos.toByteArray(); 
	}

	/**
	 * Returns a Camacized byte array, de-Carmacized.
	 * Named after John D. Carmack, this will decompress a series of
	 * bytes encoded in the Carmacizing algorithm.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] decarmacize(byte[] b)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < b.length; i++)
		{
			if (b[i] == -1)
			{
				i++;
				byte z = b[i];
				i++;
				int x = b[i] < 0? b[i] + 256 : b[i];
				for (int j = 0; j < x; j++)
					bos.write(z);
			}
			else
				bos.write(b[i]);
		}
		return bos.toByteArray();
	}
	
}
