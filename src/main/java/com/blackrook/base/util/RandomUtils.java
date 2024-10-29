/*******************************************************************************
 * Copyright (c) 2019-2024 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.lang.reflect.Array;
import java.util.Random;

/**
 * Utility class for random generation.
 * @author Matthew Tropiano
 */
public final class RandomUtils
{
	// Can't instantiate.
	private RandomUtils() {}

	/**
	 * Returns a random boolean.
	 * @param rand the random number generator.
	 * @return true or false.
	 */
	public static boolean randBoolean(Random rand)
	{
		return rand.nextBoolean();
	}

	/**
	 * Returns a random byte value.
	 * @param rand the random number generator.
	 * @return a value from 0 to 255.
	 */
	public static byte randByte(Random rand)
	{
		return (byte)randInt(rand,-128,127);
	}

	/**
	 * @param rand the random number generator.
	 * @return a random integer.
	 */
	public static int randInt(Random rand)
	{
		return rand.nextInt();
	}

	/**
	 * @param rand the random number generator.
	 * @return a random long.
	 */
	public static long randLong(Random rand)
	{
		return rand.nextLong();
	}

	/**
	 * @param rand the random number generator.
	 * @return a random float value from [0 to 1) (inclusive/exclusive).
	 */
	public static float randFloat(Random rand)
	{
		return rand.nextFloat();
	}

	/**
	 * @param rand the random number generator.
	 * @return a random double value from [0 to 1) (inclusive/exclusive).
	 */
	public static double randDouble(Random rand)
	{
		return rand.nextDouble();
	}

	/**
	 * @param rand the random number generator.
	 * @return a random Gaussian-distributed double value from -inf to +inf.
	 */
	public static double randGauss(Random rand)
	{
		return rand.nextGaussian();
	}

	/**
	 * Fills an array with random byte values.
	 * @param rand the random number generator.
	 * @param b the output array to fill with bytes.
	 */
	public static void randBytes(Random rand, byte[] b)
	{
		rand.nextBytes(b);
	}

	/**
	 * Returns a random integer from 0 (inclusive) to x (exclusive).
	 * @param rand the random number generator.
	 * @param x the upper bound.
	 * @return the next integer.
	 */
	public static int rand(Random rand, int x)
	{
		return rand.nextInt(x);
	}

	/**
	 * Returns a random integer from base to base+range.
	 * @param rand the random number generator.
	 * @param base the lower bound.
	 * @param range the upper bound (inclusive) of how much to add to the lower bound. 
	 * @return the next integer.
	 */
	public static int rand(Random rand, int base, int range)
	{
		return rand(rand,range+1)+base;
	}

	/**
	 * Returns a random float from lo to hi (inclusive).
	 * @param rand the random number generator.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the next float.
	 */
	public static float randFloat(Random rand, float lo, float hi)
	{
		return (hi - lo)*randFloat(rand) + lo;
	}

	/**
	 * Returns a random Gaussian-distributed float value from -inf to +inf.
	 * @param rand the random number generator.
	 * @return the next float.
	 */
	public static float randGaussFloat(Random rand)
	{
		return (float)randGauss(rand);
	}

	/**
	 * Returns a random Gaussian float from lo to hi (inclusive).
	 * @param rand the random number generator.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the next float.
	 */
	public static float randGaussFloat(Random rand, float lo, float hi)
	{
		float val = (hi - lo) * randGaussFloat(rand) + lo;
		return Math.max(Math.min(val, hi), lo);
	}

	/**
	 * Returns a random integer from lo to hi (inclusive).
	 * @param rand the random number generator.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the next float.
	 */
	public static int randInt(Random rand, int lo, int hi)
	{
		return rand(rand,hi-lo+1)+lo;
	}

	/**
	 * Returns a random float value from -1 to 1 (inclusive).
	 * @param rand the random number generator.
	 * @return the next float.
	 */
	public static float randFloatN(Random rand)
	{
		return randFloat(rand) * (randBoolean(rand)?-1.0f:1.0f);
	}

	/**
	 * Returns a random double from lo to hi (inclusive).
	 * @param rand the random number generator.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the next double.
	 */
	public static double randDouble(Random rand, double lo, double hi)
	{
		return (hi - lo) * randDouble(rand) + lo;
	}

	/**
	 * Returns a random double value from -1 to 1 (inclusive).
	 * @param rand the random number generator.
	 * @return the next double.
	 */
	public static double randDoubleN(Random rand)
	{
		return randDouble(rand) * (randBoolean(rand)? -1.0 : 1.0);
	}

	/**
	 * Returns a random short from lo to hi (inclusive).
	 * @param rand the random number generator.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the next short.
	 */
	public static short randShort(Random rand, int lo, int hi)
	{
		return (short)(rand(rand,hi-lo+1)+lo);
	}

	/**
	 * Returns a random short from lo to hi (inclusive).
	 * @param rand the random number generator.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the next short.
	 */
	public static short randShort(Random rand, short lo, short hi)
	{
		return (short)(rand(rand,hi-lo+1)+lo);
	}

	/**
	 * Returns a random character in a string.
	 * @param rand the Random instance to use. 
	 * @param chars the characters to sample.
	 * @return a random character.
	 */
	public static char randChar(Random rand, CharSequence chars)
	{
		return chars.charAt(rand(rand, chars.length()));
	}

	/**
	 * Generates a string of random characters from a provided alphabet.
	 * @param rand the random number generator.
	 * @param alphabet the source alphabet (cannot be null).
	 * @param length the amount of characters to generate.
	 * @return a string of sampled characters.
	 */
	public static String randString(Random rand, final String alphabet, int length)
	{
		StringBuilder sb = new StringBuilder();
		while (length-- > 0)
			sb.append(randChar(rand, alphabet));
		return sb.toString();
	}

	/**
	 * Returns a random entry in an array/list.
	 * @param <T> the array object type.   
	 * @param rand the Random instance to use. 
	 * @param objects the array of objects to select from.
	 * @return a random entry from the array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T randElement(Random rand, T ... objects)
	{
		return objects[rand(rand, objects.length)];
	}

	/**
	 * Calculates a percent chance of something occurring.
	 * @param rand the random number generator.
	 * @param percent the chance from 0 to 100.
	 * @return true if happening, false otherwise.
	 */
	public static boolean percentChance(Random rand, int percent)
	{
		return randInt(rand,0,99) < percent;
	}

	/**
	 * Rolls a die.
	 * @param rand the random number generator.
	 * @param die size of the die.
	 * @return the outcome.
	 */
	public static int roll(Random rand, int die)
	{
		if (die <= 1) return 1;
		return randInt(rand,1,die);
	}

	/**
	 * Rolls a die many times. Example: 2d20 = roll(rand,2,20)
	 * @param rand the random number generator.
	 * @param n times to roll.
	 * @param die size of the die.
	 * @return the outcome total.
	 */
	public static int roll(Random rand, int n, int die)
	{
		int total = 0;
		for (int i = 0; i < n; i++)
			total += roll(rand,die);
		return total;
	}
	
	/**
	 * Shuffles an array of elements in-place, 
	 * such that the order of the array's contents are now randomized.
	 * @param <T> the array element type.
	 * @param rand the random number generator.
	 * @param elements the array of elements.
	 */
	public static <T> void shuffle(Random rand, T[] elements)
	{
		for (int i = elements.length - 1; i > 0; i--)
		{
			int src = rand.nextInt(i);
			T temp = elements[i];
			elements[i] = elements[src];
			elements[src] = temp;
		}
	}
	
	/**
	 * Copies an array's contents, and then shuffles that new array of elements in-place, 
	 * such that the order of the array's contents are now randomized.
	 * @param <T> the array element type.
	 * @param rand the random number generator.
	 * @param elementsSource the source array of elements. 
	 * @return a copy of the source array with its contents shuffled. 
	 */
	public static <T> T[] shuffleCopy(Random rand, T[] elementsSource)
	{
		if (elementsSource.length == 0)
			return elementsSource;
		
		@SuppressWarnings("unchecked")
		T[] out = (T[])Array.newInstance(elementsSource[0].getClass(), elementsSource.length);
		System.arraycopy(elementsSource, 0, out, 0, elementsSource.length);
		shuffle(rand, out);
		return out;
	}
	
}
