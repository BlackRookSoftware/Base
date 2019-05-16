/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

/**
 * Simple bit functions.
 * @author Matthew Tropiano
 */
public final class BitUtils
{
	private BitUtils() {}

	/**
	 * Checks if bits are set in a value.
	 * @param value		the value.
	 * @param test		the testing bits.
	 * @return			true if all of the bits set in test are set in value, false otherwise.
	 */
	public static boolean bitIsSet(long value, long test)
	{
		return (value & test) == test;
	}

	/**
	 * Sets the bits of a value.
	 * @param value		the value.
	 * @param bits		the bits to set.
	 * @return			the resulting number.
	 */
	public static long setBits(long value, long bits)
	{
		return value | bits;
	}

	/**
	 * Sets the bits of a value.
	 * @param value		the value.
	 * @param bits		the bits to set.
	 * @return			the resulting number.
	 */
	public static int setBits(int value, int bits)
	{
		return value | bits;
	}

	/**
	 * Clears the bits of a value.
	 * @param value		the value.
	 * @param bits		the bits to clear.
	 * @return			the resulting number.
	 */
	public static long clearBits(long value, long bits)
	{
		return value & ~bits;
	}

	/**
	 * Clears the bits of a value.
	 * @param value		the value.
	 * @param bits		the bits to clear.
	 * @return			the resulting number.
	 */
	public static int clearBits(int value, int bits)
	{
		return value & ~bits;
	}

	/**
	 * Converts a series of boolean values to bits,
	 * going from least-significant to most-significant.
	 * TRUE booleans set the bit, FALSE ones do not.
	 * @param bool list of booleans. cannot exceed 32.
	 * @return the resultant bitstring in an integer.
	 */
	public static int booleansToInt(boolean ... bool)
	{
		int out = 0;
		for (int i = 0; i < Math.min(bool.length, 32); i++)
			if (bool[i])
				out |= (1 << i);
		return out;
	}

	/**
	 * Converts a series of boolean values to bits,
	 * going from least-significant to most-significant.
	 * TRUE booleans set the bit, FALSE ones do not.
	 * @param bool list of booleans. cannot exceed 64.
	 * @return the resultant bitstring in a long integer.
	 */
	public static long booleansToLong(boolean ... bool)
	{
		int out = 0;
		for (int i = 0; i < Math.min(bool.length, 64); i++)
			if (bool[i])
				out |= (1 << i);
		return out;
	}
	
}
