/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.IOException;
import java.io.InputStream;

import com.blackrook.base.util.BitUtils;
import com.blackrook.base.util.BufferUtils;
import com.blackrook.base.util.SerializerUtils;

/**
 * Assists in endian reading and other special serializing stuff.
 * @author Matthew Tropiano
 */
public class SerialReader implements AutoCloseable
{
    private final byte[] singleByteBuffer = new byte[1];

    public static final int
    END_OF_STREAM = 0xffffffff;
    
	public static final boolean
	LITTLE_ENDIAN =	true,
	BIG_ENDIAN = false;

	/** InputStream for reading. */
	private InputStream in;
	/** Endian mode switch. */
	private boolean endianMode;
	
	private int bitsLeft;
	private static byte[] BITMASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80};
	private byte currentBitByte;

	/**
	 * Wraps a super reader around an InputStream.  
	 * @param i				the input stream to use.
	 * @param endianMode	the endian mode to use.
	 */
	public SerialReader(InputStream i, boolean endianMode)
	{
		in = i;
		setEndianMode(endianMode);
		byteAlign();
	}
	
	/**
	 * Sets the byte endian mode for the byte conversion methods.
	 * LITTLE_ENDIAN (Intel), the default, orients values from lowest byte to highest, while
	 * BIG_ENDIAN (Motorola, VAX) orients values from highest byte to lowest.
	 * @param mode	an _ENDIAN mode.
	 */
	public void setEndianMode(boolean mode)
	{
		endianMode = mode;
	}
	
	/**
	 * Reads a byte from the bound stream.
	 * @return	the byte read or END_OF_STREAM if the end of the stream is reached.
	 */
	protected synchronized int byteRead() throws IOException
	{
		byteAlign();
		return in.read();
	}
	
	/**
	 * Reads a series of bytes from the bound stream into a byte array until end of 
	 * stream is reached or the array is filled with bytes.
	 * @param b 		the target array to fill with bytes.
	 * @return	the amount of bytes read or END_OF_STREAM if the end of the stream 
	 * 			is reached before a single byte is read.
	 */
	protected int byteRead(byte[] b) throws IOException
	{
		return byteRead(b,b.length);
	}

	/**
	 * Reads a series of bytes from the bound stream into a byte array until end of 
	 * stream is reached or <code>maxlen</code> bytes have been read.
	 * @param b 		the target array to fill with bytes.
	 * @param maxlen	the maximum amount of bytes to read.
	 * @return	the amount of bytes read or END_OF_STREAM if the end of the stream 
	 * 			is reached before a single byte is read.
	 */
	protected int byteRead(byte[] b, int maxlen) throws IOException
	{
		byteAlign();
		return in.read(b, 0, maxlen);
	}

	// Casts a short to a char.
	private char shortToChar(short s)
	{
	    return (char)(s & 0xFFFF);
	}

	/**
	 * Keeps reading until it hits a specific byte pattern.
	 * Returns true if the pattern is found, returns false if the end of the stream
	 * is reached before the pattern is matched.
	 */
	public boolean seekToPattern(byte[] b) throws IOException
	{
		int x = b.length;
		int i = 0;
		while (i < x)
		{
			int buf = byteRead(singleByteBuffer);
			if (buf < 1)
				return false;
			if (singleByteBuffer[0] == b[i])
				i++;
			else
				i = 0;
		}
		return true;
	}
	
	/**
	 * Reads a bunch of bytes and checks to see if a set bytes match completely
	 * with the input byte string. It reads up to the length of b before it starts the check.
	 * @param b	the input byte string.
	 * @return true if the bytes read equal the the same bytes in the input array.
	 */
	public boolean readFor(byte[] b) throws IOException
	{
		byte[] read = new byte[b.length];
		byteRead(read);
		for (int i = 0; i < b.length; i++)
			if (read[i] != b[i])
				return false;
		return true;
	}
	
	/**
	 * If we started reading bits, this will align the reader to the next byte.
	 * If this is called when we are at the beginning of the next byte, it doesn't do anything.
	 * WARNING: This is the only method that cares about the current bits. If you start reading bits,
	 * the other methods will continue at the next byte.
	 */
	public void byteAlign()
	{
		bitsLeft = 0;
	}

	/**
	 * Reads a bit. Reads from least significant bit to most significant bit of the current byte.
	 * @return	true if set, false if not.
	 * @throws IOException	if the bit cannot be read.
	 */
	public boolean readBit() throws IOException
	{
		if (bitsLeft == 0)
		{
			currentBitByte = readByte();
		    bitsLeft = 8;
		}
		return (BITMASK[BITMASK.length - (bitsLeft--)] & currentBitByte) != 0;
	}

	/**
	 * Reads a set of bits in and returns it as an int.
	 * @throws IllegalArgumentException if bits is less than zero or greater than 32.
	 */
	public int readIntBits(int bits) throws IOException
	{
		if (bits < 0 || bits > 32)
			throw new IllegalArgumentException("Bits should be between 0 and 32.");

		int out = 0;
		int i = 0;
		while ((bits--) > 0)
		{
			if (readBit())
				out |= (1 << i);
			i++;
		}
		return out;
	}
	
	/**
	 * Reads a set of bits in and returns it as a long.
	 * @throws IllegalArgumentException if bits is less than zero or greater than 64.
	 */
	public long readLongBits(int bits) throws IOException
	{
		if (bits < 0 || bits > 64)
			throw new IllegalArgumentException("Bits should be between 0 and 64.");

		long out = 0;
		int i = 0;
		while ((bits--) > 0)
		{
			if (readBit())
				out |= (1 << i);
			i++;
		}
		return out;
	}
	
	/**
	 * Reads a byte array in from the reader.
	 * @return an array of bytes
	 * @throws IOException if the end of the stream is reached prematurely.
	 */
	public byte[] readByteArray() throws IOException
	{
		byte[] out = new byte[readInt()];
	    if (out.length == 0)
	    	return out;
	    int buf = byteRead(out);
	    if (buf < out.length)
	        throw new IOException("Not enough bytes for byte array.");
	    return out;
	}

	/**
	 * Reads a char array and returns it as a String.
	 * @return the resulting String.
	 * @throws IOException if an I/O error occurs.
	 */
	public String readString() throws IOException
	{
		char[] c = readCharArray();
	    return new String(c);
	}

	/**
	 * Reads a byte vector (an int followed by a series of bytes) and returns it as a String
	 * in a particular encoding.
	 * @param encoding	the name of the encoding scheme.
	 * @throws IOException if an I/O error occurs.
	 */
	public String readString(String encoding) throws IOException
	{
		byte[] b = readByteArray();
	    return new String(b, encoding);
	}

	/**
	 * Reads in an array of strings.
	 * Basically reads an integer length which is the length of the array and then reads that many strings.
	 * @throws IOException	if an error occurred during the read.
	 */
	public String[] readStringArray() throws IOException
	{
	    String[] out = new String[readInt()];
	    for (int i = 0; i < out.length; i++)
	        out[i] = readString();
	    return out;
	}

	/**
	 * Reads in a boolean value stored as a single byte.
	 * @throws IOException	if an error occurred during the read.
	 */
	public boolean readBoolean() throws IOException
	{
	    byte[] buffer = new byte[1];	    
	    int buf = byteRead(buffer);
	    if (buf < 1) 
	    	throw new IOException("Not enough bytes for a boolean.");
	    return (buffer[0] != 0) ? true : false;
	}

	/**
	 * Reads in an array of boolean values.
	 * Basically reads an integer length which is the amount of booleans and then reads 
	 * in an integer at a time scanning bits for the boolean values.
	 * @throws IOException	if an error occurred during the read.
	 */
	public boolean[] readBooleanArray() throws IOException
	{
		boolean[] out = new boolean[readInt()];
			
		int currint = 0;
		for (int i = 0; i < out.length; i++)
		{
			if (i%Integer.SIZE == 0)
				currint = readInt();
			
			out[i] = BitUtils.bitIsSet(currint,(1<<(i%Integer.SIZE)));
		}
		return out;
	}

	/**
	 * Reads in a long value.
	 * @throws IOException	if an error occurred during the read.
	 */
	public long readLong() throws IOException
	{
	    byte[] buffer = new byte[BufferUtils.SIZEOF_LONG];
	    int buf = byteRead(buffer);
	    if (buf < BufferUtils.SIZEOF_LONG) 
	    	throw new IOException("Not enough bytes for a long.");
	    return SerializerUtils.bytesToLong(buffer, endianMode);
	}

	/**
	 * Reads in an amount of long values specified by the user.
	 * @throws IOException	if an error occurred during the read.
	 */
	public long[] readLongs(int n) throws IOException
	{
	    long[] out = new long[n];
	    for (int i = 0; i < out.length; i++)
	    	out[i] = readLong();
	    return out;
	}

	/**
	 * Reads in an array of long values.
	 * Basically reads an integer length which is the length of the array and then reads that many longs.
	 * @throws IOException	if an error occurred during the read.
	 */
	public long[] readLongArray() throws IOException
	{
		return readLongs(readInt());
	}

	/**
	 * Reads in a single byte.
	 * @throws IOException	if an error occurred during the read.
	 */
	public byte readByte() throws IOException
	{
	    int buf = byteRead(singleByteBuffer);
	    if (buf < 1)
	    	throw new IOException("not enough bytes");
	    else if (buf < 1) throw new IOException("Not enough bytes for a byte.");
	    return singleByteBuffer[0];
	}

	/**
	 * Reads in a single byte, cast to a short to eliminate sign.
	 * @throws IOException	if an error occurred during the read.
	 */
	public short readUnsignedByte() throws IOException
	{
	    return (short)(readByte() & 0x0ff);
	}

	/**
	 * Reads a series of bytes from the bound stream into a byte array until end of 
	 * stream is reached or the array is filled with bytes.
	 * @param b 		the target array to fill with bytes.
	 * @return	the amount of bytes read or END_OF_STREAM if the end of the stream 
	 * 			is reached before a single byte is read.
	 */
	public int readBytes(byte[] b) throws IOException
	{
		return byteRead(b);
	}

	/**
	 * Reads a series of bytes from the bound stream into a byte array until end of 
	 * stream is reached or <code>maxlen</code> bytes have been read.
	 * @param b 		the target array to fill with bytes.
	 * @param maxlen	the maximum amount of bytes to read.
	 * @return	the amount of bytes read or END_OF_STREAM if the end of the stream 
	 * 			is reached before a single byte is read.
	 */
	public int readBytes(byte[] b, int maxlen) throws IOException
	{
		return byteRead(b, maxlen);
	}

	/**
	 * Reads in a specified amount of bytes, returned as an array.
	 * @throws IOException	if an error occurred during the read.
	 */
	public byte[] readBytes(int n) throws IOException
	{
	    byte[] out = new byte[n];
	    int buf = byteRead(out);
	    if (buf < n) 
	    	throw new IOException("Not enough bytes to read.");
	    return out;
	}

	/**
	 * Reads in a integer, cast to a long, discarding sign.
	 * @throws IOException	if an error occurred during the read.
	 */
	public long readUnsignedInt() throws IOException
	{
		return readInt() & 0x0ffffffffL;
	}
	
	/**
	 * Reads in an integer.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int readInt() throws IOException
	{
	    byte[] buffer = new byte[BufferUtils.SIZEOF_INT];
	    int buf = byteRead(buffer);
	    if (buf < BufferUtils.SIZEOF_INT) 
	    	throw new IOException("Not enough bytes for an int.");
	    return SerializerUtils.bytesToInt(buffer, endianMode);
	}

	/**
	 * Reads in a 24-bit integer.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int read24BitInt() throws IOException
	{
		byte[] bbu = new byte[3];
	    byte[] buffer = new byte[BufferUtils.SIZEOF_INT];
	    int buf = byteRead(bbu,3);
	    if (buf < 3)
	    	throw new IOException("not enough bytes");
	    else if (buf < bbu.length) throw new IOException("Not enough bytes for a 24-bit int.");
	    if (endianMode == BIG_ENDIAN)
	    	System.arraycopy(bbu, 0, buffer, 1, 3);
	    else if (endianMode == LITTLE_ENDIAN)
	    	System.arraycopy(bbu, 0, buffer, 0, 3);
	    return SerializerUtils.bytesToInt(buffer, endianMode);
	}

	/**
	 * Reads in a specified amount of integers.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int[] readInts(int n) throws IOException
	{
	    int[] out = new int[n];
	    for (int i = 0; i < out.length; i++)
	    	out[i] = readInt();
	    return out;
	}

	/**
	 * Reads in an array of integers.
	 * Basically reads an integer length which is the length of the array and then reads that many integers.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int[] readIntArray() throws IOException
	{
	    return readInts(readInt());
	}

	/**
	 * Reads in an array of arrays of integers.
	 * Basically reads an integer length which is the length of the array and then reads that many integer arrays.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int[][] readDoubleIntArray() throws IOException
	{
	    int[][] out = new int[readInt()][];
	    for (int i = 0; i < out.length; i++)
	        out[i] = readIntArray();
	    return out;	    
	}

	/**
	 * Reads in an array of arrays of arrays of integers.
	 * Basically reads an integer length which is the length of the array and then reads that many arrays of integer arrays.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int[][][] readTripleIntArray() throws IOException
	{
	    int[][][] out = new int[readInt()][][];
	    for (int i = 0; i < out.length; i++)
	        out[i] = readDoubleIntArray();
	    return out;	    
	}

	/**
	 * Reads in a 32-bit float.
	 * @throws IOException	if an error occurred during the read.
	 */
	public float readFloat() throws IOException
	{
	    byte[] buffer = new byte[BufferUtils.SIZEOF_FLOAT];
	    int buf = byteRead(buffer);
	    if (buf < BufferUtils.SIZEOF_FLOAT) 
	    	throw new IOException("Not enough bytes for a float.");
	    return SerializerUtils.bytesToFloat(buffer, endianMode);
	}

	/**
	 * Reads in a specified amount of 32-bit floats.
	 * @throws IOException	if an error occurred during the read.
	 */
	public float[] readFloats(int n) throws IOException
	{
	    float[] out = new float[n];
	    for (int i = 0; i < out.length; i++)
	    	out[i] = readFloat();
	    return out;
	}

	/**
	 * Reads in an array 32-bit floats.
	 * Basically reads an integer length which is the length of the array and then reads that many floats.
	 * @throws IOException	if an error occurred during the read.
	 */
	public float[] readFloatArray() throws IOException
	{
	    return readFloats(readInt());
	}

	/**
	 * Reads in a 64-bit float.
	 * @throws IOException	if an error occurred during the read.
	 */
	public double readDouble() throws IOException
	{
	    byte[] buffer = new byte[BufferUtils.SIZEOF_DOUBLE];
	    int buf = byteRead(buffer);
	    if (buf < BufferUtils.SIZEOF_DOUBLE) 
	    	throw new IOException("Not enough bytes for a double.");
	    return Double.longBitsToDouble(SerializerUtils.bytesToLong(buffer, endianMode));
	}

	/**
	 * Reads in a specified amount of 64-bit floats.
	 * @throws IOException	if an error occurred during the read.
	 */
	public double[] readDoubles(int n) throws IOException
	{
	    double[] out = new double[n];
	    for (int i = 0; i < out.length; i++)
	    	out[i] = readDouble();
	    return out;
	}

	/**
	 * Reads in an array 64-bit floats.
	 * Basically reads an integer length which is the length of the array and then reads that many doubles.
	 * @throws IOException	if an error occurred during the read.
	 */
	public double[] readDoubleArray() throws IOException
	{
	    return readDoubles(readInt());
	}

	/**
	 * Reads in a short.
	 * @throws IOException	if an error occurred during the read.
	 */
	public short readShort() throws IOException
	{
	    byte[] buffer = new byte[BufferUtils.SIZEOF_SHORT];
	    int buf = byteRead(buffer);
	    if (buf < BufferUtils.SIZEOF_SHORT) 
	    	throw new IOException("Not enough bytes for a short.");
	    return SerializerUtils.bytesToShort(buffer, endianMode);
	}

	/**
	 * Reads in a short, cast to an integer, discarding sign.
	 * @throws IOException	if an error occurred during the read.
	 */
	public int readUnsignedShort() throws IOException
	{
		return readShort() & 0x0ffff;
	}
	
	/**
	 * Reads in a specified amount of shorts.
	 * @throws IOException	if an error occurred during the read.
	 */
	public short[] readShorts(int n) throws IOException
	{
	    short[] out = new short[n];
	    for (int i = 0; i < out.length; i++)
	    	out[i] = readShort();
	    return out;
	}

	/**
	 * Reads in an array of shorts.
	 * Basically reads an integer length which is the length of the array and then reads that many shorts.
	 * @throws IOException	if an error occurred during the read.
	 */
	public short[] readShortArray() throws IOException
	{
	    return readShorts(readInt());
	}

	/**
	 * Reads in an array of arrays of shorts.
	 * Basically reads an integer length which is the length of the array and then reads that many arrays of shorts.
	 * @throws IOException	if an error occurred during the read.
	 */
	public short[][] readDoubleShortArray() throws IOException
	{
	    short[][] out = new short[readInt()][];
	    for (int i = 0; i < out.length; i++)
	        out[i] = readShortArray();
	    return out;
	}

	/**
	 * Reads in an array of arrays of arrays of shorts.
	 * Basically reads an integer length which is the length of the array and then reads that many arrays of arrays of shorts.
	 * @throws IOException	if an error occurred during the read.
	 */
	public short[][][] readTripleShortArray() throws IOException
	{
	    short[][][] out = new short[readInt()][][];
	    for (int i = 0; i < out.length; i++)
	        out[i] = readDoubleShortArray();
	    return out;
	}

	/**
	 * Reads in a character.
	 * @throws IOException	if an error occurred during the read.
	 */
	public char readChar() throws IOException
	{
	    return shortToChar(readShort());
	}

	/**
	 * Reads in a specific amount of characters.
	 * @throws IOException	if an error occurred during the read.
	 */
	public char[] readChars(int n) throws IOException
	{
	    char[] out = new char[n];
	    for (int i = 0; i < out.length; i++)
	    	out[i] = readChar();
	    return out;
	}

	/**
	 * Reads in an array of characters.
	 * Basically reads an integer length which is the length of the array and then reads that many characters.
	 * @throws IOException	if an error occurred during the read.
	 */
	public char[] readCharArray() throws IOException
	{
	    short[] s = readShortArray();
	    char[] out = new char[s.length];
	    for (int i = 0; i < s.length; i++)
	        out[i] = shortToChar(s[i]);
	    return out;
	}

	/**
	 * Reads an integer from an input stream that is variable-length encoded.
	 * Reads up to four bytes. Due to the nature of this value, it is always
	 * read in a Big-Endian fashion.
	 * @return an int value from 0x00000000 to 0x0FFFFFFF.
	 * @throws IOException if the next byte to read is not available.
	 */
	public int readVariableLengthInt() throws IOException
	{
		int out = 0;
		byte b = 0;
		do {
			b = readByte();
			out |= b & 0x7f;
			if ((b & 0x80) != 0)
				out <<= 7;
		} while ((b & 0x80) != 0);
		return out;
	}

	/**
	 * Reads a long from an input stream that is variable-length encoded.
	 * Reads up to eight bytes. Due to the nature of this value, it is always
	 * read in a Big-Endian fashion.
	 * @return a long value from 0x00000000 to 0x7FFFFFFFFFFFFFFF.
	 * @throws IOException if the next byte to read is not available.
	 */
	public long readVariableLengthLong() throws IOException
	{
		long out = 0;
		byte b = 0;
		do {
			b = readByte();
			out |= b & 0x7f;
			if ((b & 0x80) != 0)
				out <<= 7;
		} while ((b & 0x80) != 0);
		return out;
	}

	/**
	 * Closes the bound input stream.
	 * $throws IOException if an error occurred.
	 */
	public final void close() throws IOException
	{
		in.close();
	}

}
