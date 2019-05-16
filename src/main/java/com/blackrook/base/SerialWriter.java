/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.IOException;
import java.io.OutputStream;

import com.blackrook.base.util.SerializerUtils;

/**
 * Assists in endian reading and other special serializing stuff.
 * @author Matthew Tropiano
 */
public class SerialWriter implements AutoCloseable
{
    private final byte[] singleByteBuffer = new byte[1];

    public static final boolean
	LITTLE_ENDIAN =	true,
	BIG_ENDIAN = false;

	/** OutputStream for reading. */
	private OutputStream out;
	/** Endian mode switch. */
	private boolean endianMode;

	private int bitsLeft;
	private static byte[] BITMASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80};
	private byte currentBitByte;

	/**
	 * Wraps a super writer around an OutputStream.  
	 * @param o				the output stream to use.
	 * @param endianMode	the endian mode to use.
	 */
	public SerialWriter(OutputStream o, boolean endianMode)
	{
		out = o;
		setEndianMode(endianMode);
		bitsLeft = 8;
	}
	
	/**
	 * Sets the byte endian mode for the byte conversion methods.
	 * LITTLE_ENDIAN (Intel), the default, orients values from lowest byte to highest, while
	 * BIG_ENDIAN (Motorola) orients values from highest byte to lowest.
	 * @param mode	an _ENDIAN mode.
	 */
	public void setEndianMode(boolean mode)
	{
		endianMode = mode;
	}
	
	/**
	 * Casts a char to a short.
	 */
	private short charToShort(char c)
	{
	    return (short)(c & 0xFFFF);
	}

	/**
	 * Writes a String to the bound output stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeString(String s) throws IOException
	{
		writeCharArray(s.toCharArray());
	}

	/**
	 * Writes a String to the bound output stream in a
	 * specific encoding.
	 * @param s				the String to write.
	 * @param encodingType	the encoding type name.
	 */
	public void writeString(String s, String encodingType) throws IOException
	{
		writeByteArray(s.getBytes(encodingType));
	}
	
	/**
	 * Writes an array of Strings to the bound output stream,
	 * which is the length of the array as an integer plus each String.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeStringArray(String[] s) throws IOException
	{
		writeInt(s.length);
		for (int i = 0; i < s.length; i++)
			writeString(s[i]);
	}

	/**
	 * Flushes the bit buffer used for bit-writing.
	 * @throws IOException	if the bits cannot be written.
	 */
	public void flushBits() throws IOException
	{
		writeByte(currentBitByte);
		currentBitByte = 0;
		bitsLeft = 8;
	}

    /**
	 * Writes a bit. Writes least significant bit to most significant bit of the current byte.
	 * @throws IOException	if the bit cannot be written.
	 */
	public void writeBit(boolean bit) throws IOException
	{
		if (bitsLeft == 0)
			flushBits();
		if (bit)
			currentBitByte |= BITMASK[BITMASK.length - bitsLeft];
		bitsLeft--;
	}

	/**
	 * Writes a set of bits to the bit buffer.
	 * @throws IllegalArgumentException if bits is less than zero or greater than 32.
	 */
	public void writeIntBits(int bitcount, int bits) throws IOException
	{
		if (bits < 0 || bits > 32)
			throw new IllegalArgumentException("Bits should be between 0 and 32.");
	
		int i = 0;
		while ((bits--) > 0)
			writeBit((bits & (1 << i++)) != 0);
	}

	/**
	 * Writes a set of bits to the bit buffer.
	 * @throws IllegalArgumentException if bits is less than zero or greater than 64.
	 */
	public void writeLongBits(int bitcount, long bits) throws IOException
	{
		if (bits < 0 || bits > 64)
			throw new IllegalArgumentException("Bits should be between 0 and 64.");
	
		int i = 0;
		while ((bits--) > 0)
			writeBit((bits & (1 << i++)) != 0);
	}

	/**
	 * Writes a byte.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeByte(byte b) throws IOException
	{
		singleByteBuffer[0] = b;
		out.write(singleByteBuffer);
	}

	/**
	 * Writes a short that is less than 256 to a byte.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeUnsignedByte(short b) throws IOException
	{
		writeByte((byte)(b & 0x0ff));
	}

	/**
	 * Writes an int that is less than 256 to a byte.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeUnsignedByte(int b) throws IOException
	{
		writeByte((byte)(b & 0x0ff));
	}

	/**
	 * Writes a series of bytes to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeBytes(byte[] b) throws IOException
	{
		out.write(b);
	}

	/**
	 * Writes an array of bytes to the bound stream,
	 * which is the length of the array as an integer plus each byte.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeByteArray(byte[] b) throws IOException
	{
		writeInt(b.length);
		out.write(b);
	}

	/**
	 * Writes a boolean to the bound stream as a byte.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeBoolean(boolean b) throws IOException
	{
		writeByte((byte)(b?1:0));
	}

	/**
	 * Writes a long that is less than 2^32 to an integer.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeUnsignedInteger(long l) throws IOException
	{
		writeInt((int)(l & 0x0ffffffffL));
	}

	/**
	 * Writes an integer to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeInt(int i) throws IOException
	{
		byte[] buffer = Cache.LOCAL.get().buffer;
		SerializerUtils.intToBytes(i, endianMode, buffer, 0);
		out.write(buffer, 0, 4);
	}

	/**
	 * Converts an integer from an int to a variable-length string of bytes.
	 * Makes up to four bytes. Due to the nature of this algorithm, it is always
	 * written out in a Big-Endian fashion.
	 * @param i	the int to convert.
	 * @throws IllegalArgumentException	if the int value to convert is above 0x0fffffff.
	 */
	public void writeVariableLengthInt(int i) throws IOException
	{
		if ((i & 0xf0000000) != 0)
			throw new IllegalArgumentException("Int value out of bounds.");
		if (i == 0)
		{
			out.write(0);
			return;
		}
		byte[] b;
		int z = i, x = 0;
		while (z > 0) {z >>= 7; x++;}
		b = new byte[x];
		for (int n = x-1; n >= 0; n--)
		{
			b[n] = (byte)(i & 0x7f);
			i >>= 7;
	    	if (n != x-1)
	    		b[n] |= (byte)(0x80);
		}
		out.write(b);
	}

	/**
	 * Converts a long from a long to a variable-length string of bytes.
	 * Makes up to eight bytes. Due to the nature of this algorithm, it is always
	 * written out in a Big-Endian fashion.
	 * @param i	the long to convert.
	 * @throws IllegalArgumentException	if the long value to convert is above 0x7fffffffffffffffL.
	 */
	public void writeVariableLengthLong(long i) throws IOException
	{
		if ((i & 0x8000000000000000L) != 0)
			throw new IllegalArgumentException("Long value too large.");
		if (i == 0)
		{
			out.write(0);
			return;
		}
		byte[] b;
		long z = i;
		int x = 0;
		while (z > 0) {z >>= 7; x++;}
		b = new byte[x];
		
		for (int n = x-1; n >= 0; n--)
		{
			b[n] = (byte)(i & 0x7f);
			i >>= 7;
	    	if (n != x-1)
	    		b[n] |= (byte)(0x80);
		}
		out.write(b);
	}

	/**
	 * Writes an integer array to the bound stream,
	 * which is the length of the array as an integer plus each integer.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeIntArray(int[] i) throws IOException
	{
		writeInt(i.length);
	    for (int x = 0; x < i.length; x++)
	    	writeInt(i[x]);
	}

	/**
	 * Writes an array of integer arrays to the bound stream,
	 * which is the length of the array as an integer plus each integer array.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeIntArray(int[][] i) throws IOException
	{
		writeInt(i.length);
		for (int x = 0; x < i.length; x++)
			writeIntArray(i[x]);
	}

	/**
	 * Writes an array of arrays of integer arrays to the bound stream,
	 * which is the length of the array as an integer plus each array of integer arrays.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeIntArray(int[][][] i) throws IOException
	{
		writeInt(i.length);
		for (int x = 0; x < i.length; x++)
			writeIntArray(i[x]);
	}

	/**
	 * Writes a long to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeLong(long l) throws IOException
	{
		byte[] buffer = Cache.LOCAL.get().buffer;
		SerializerUtils.longToBytes(l, endianMode, buffer, 0);
		out.write(buffer);
	}

	/**
	 * Writes an array of longs to the bound stream,
	 * which is the length of the array as an integer plus each long.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeLongArray(long[] l) throws IOException
	{
		writeInt(l.length);
		for (int x = 0; x < l.length; x++)
			writeLong(l[x]);
	}

	/**
	 * Writes an array of 32-bit floats to the bound stream,
	 * which is the length of the array as an integer plus each float.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeFloatArray(float[] f) throws IOException
	{	
		writeInt(f.length);
		for (int x = 0; x < f.length; x++)
			writeFloat(f[x]);
	}

	/**
	 * Writes a 32-bit float to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeFloat(float f) throws IOException
	{
	    writeInt(Float.floatToIntBits(f));
	}

	/**
	 * Writes a 64-bit float to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeDouble(double d) throws IOException
	{
	    writeLong(Double.doubleToLongBits(d));
	}

	/**
	 * Writes an array of 64-bit floats to the bound stream,
	 * which is the length of the array as an integer plus each double.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeDoubleArray(double[] d) throws IOException
	{	
		writeInt(d.length);
		for (int x = 0; x < d.length; x++)
			writeDouble(d[x]);
	}

	/**
	 * Writes a short to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeShort(short s) throws IOException
	{
		byte[] buffer = Cache.LOCAL.get().buffer;
		SerializerUtils.shortToBytes(s, endianMode, buffer, 0);
		out.write(buffer, 0, 2);
	}

	/**
	 * Writes an integer, less than 65536, as a short to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeUnsignedShort(int s) throws IOException
	{
		writeShort((short)(s & 0x0ffff));
	}

	/**
	 * Writes an array of shorts to the bound stream,
	 * which is the length of the array as an integer plus each short.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeShortArray(short[] s) throws IOException
	{
		writeInt(s.length);
		for (int x = 0; x < s.length; x++)
			writeShort(s[x]);
	}

	/**
	 * Writes an array of arrays of shorts to the bound stream,
	 * which is the length of the array as an integer plus each array of shorts.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeShortArray(short[][] s) throws IOException
	{
		writeInt(s.length);
		for (int x = 0; x < s.length; x++)
			writeShortArray(s[x]);
	}

	/**
	 * Writes an array of arrays of arrays of shorts to the bound stream,
	 * which is the length of the array as an integer plus each array of arrays of shorts.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeShortArray(short[][][] s) throws IOException
	{
		writeInt(s.length);
		for (int x = 0; x < s.length; x++)
			writeShortArray(s[x]);
	}

	/**
	 * Writes a character to the bound stream.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeChar(char c) throws IOException
	{
	    writeShort(charToShort(c));
	}

	/**
	 * Writes a character array to the bound stream,
	 * which is the length of the array as an integer plus each character.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeCharArray(char[] c) throws IOException
	{
		writeInt(c.length);
		for (int x = 0; x < c.length; x++)
			writeChar(c[x]);
	}

	/**
	 * Writes a boolean array to the bound stream,
	 * which is the length of the array as an integer plus each boolean grouped into integer bits.
	 * @throws IOException	if an error occurred during the write.
	 */
	public void writeBooleanArray(boolean ... b) throws IOException
	{
		int[] bbits = new int[(b.length/Integer.SIZE)+((b.length%Integer.SIZE)!=0?1:0)];
		for (int i = 0; i < b.length; i++)
			if (b[i])
				bbits[i/Integer.SIZE] |= 1 << (i%Integer.SIZE);

		writeInt(b.length);
		for (int i = 0; i < bbits.length; i++)
			writeInt(bbits[i]);
	}

	/**
	 * Closes the stream bound to this writer.
	 * @throws IOException	if an error occurs closing the stream.
	 */
	public final void close() throws IOException
	{
		out.close();
	}
	
	private static class Cache
	{
		private static final ThreadLocal<Cache> LOCAL = ThreadLocal.withInitial(()->new Cache());

		byte[] buffer;
		
		private Cache()
		{
			this.buffer = new byte[8];
		}
	}

}
