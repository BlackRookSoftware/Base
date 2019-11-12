/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

/**
 * A class with static methods that perform "other" types of mathematics.
 * <p>
 * A bunch of the collision/intersection algorithms found in this class are 
 * adapted from Real-Time Collision Detection by Christer Ericson (ISBN-13: 978-1-55860-732-3).
 * @author Matthew Tropiano
 */
public final class MathUtils
{
	/** PI over Two. Equivalent to <code> {@link Math#PI} / 2.0</code>. */
	public static final double PI_OVER_TWO = Math.PI / 2.0;
	/** Two PI. Equivalent to <code> 2.0 * {@link Math#PI}</code>. */
	public static final double TWO_PI = 2.0 * Math.PI;
	/** Three PI over Two. Equivalent to <code> 3.0 * {@link Math#PI} / 2.0</code>. */
	public static final double THREE_PI_OVER_TWO = 3.0 * Math.PI / 2.0;

	private MathUtils() {}
	
	/**
	 * Rotates the bits of a number to the left.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static int rotateLeft(int n, int x)
	{
		if (x < 0) return rotateRight(n, -x);
		x = x % 32;
		int m = ((1 << x) - 1) << (32 - x);
		return (n << x) | ((n & m) >>> (32 - x));
	}
	
	/**
	 * Rotates the bits of a number to the right.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static int rotateRight(int n, int x)
	{
		if (x < 0) return rotateLeft(n, -x);
		x = x % 32;
		int m = ((1 << x) - 1);
		return (n >>> x) | ((n & m) << (32 - x));
	}
	
	/**
	 * Rotates the bits of a number to the left.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static short rotateLeft(short n, int x)
	{
		if (x < 0) return rotateRight(n, -x);
		x = x % 16;
		short m = (short)(((1 << x) - 1) << (16 - x));
		return (short)(((n << x) | ((n & m) >>> (16 - x))) & 0x0000ffff);
	}
	
	/**
	 * Rotates the bits of a number to the right.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static short rotateRight(short n, int x)
	{
		if (x < 0) return rotateLeft(n, -x);
		x = x % 16;
		short m = (short)((1 << x) - 1);
		return (short)(((n >>> x) | ((n & m) << (16 - x))) & 0x0000ffff);
	}
	
	/**
	 * Rotates the bits of a number to the left.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static byte rotateLeft(byte n, int x)
	{
		if (x < 0) return rotateRight(n, -x);
		x = x % 8;
		byte m = (byte)(((1 << x) - 1) << (8 - x));
		return (byte)(((n << x) | ((n & m) >>> (8 - x))) & 0x000000ff);
	}
	
	/**
	 * Rotates the bits of a number to the right.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static byte rotateRight(byte n, int x)
	{
		if (x < 0) return rotateLeft(n, -x);
		x = x % 8;
		byte m = (byte)((1 << x) - 1);
		return (byte)(((n >>> x) | ((n & m) << (8 - x))) & 0x000000ff);
	}
	
	/**
	 * Rotates the bits of a number to the left.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static long rotateLeft(long n, int x)
	{
		if (x < 0) return rotateRight(n, -x);
		x = x % 64;
		long m = ((1 << x) - 1) << (64 - x);
		return (n << x) | ((n & m) >>> (64 - x));
	}
	
	/**
	 * Rotates the bits of a number to the right.
	 * @param n the input number.
	 * @param x the number of positions.
	 * @return the resultant number.
	 */
	public static long rotateRight(long n, int x)
	{
		if (x < 0) return rotateLeft(n, -x);
		x = x % 64;
		long m = (1 << x) - 1;
		return (n >>> x) | ((n & m) << (64 - x));
	}
	
	/**
	 * Finds the closest power of two to an integer value, larger than the initial value.
	 * <p>Examples:</p>
	 * <ul>
	 * <li>If x is 19, this returns 32.</li>
	 * <li>If x is 4, this returns 4.</li>
	 * <li>If x is 99, this returns 128.</li>
	 * <li>If x is 129, this returns 256.</li>
	 * </ul>
	 * @param x	the input value.
	 * @return the closest power of two.
	 */
	public static int closestPowerOfTwo(int x)
	{
		if (x <= 1)
			return 1;
		if (x == 2)
			return x;
		int out = 2;
		while (x > 1)
		{
			out <<= 1;
			x >>= 1;
		}
		return out;
	}

	/**
	 * Returns the greatest common divisor of two integers.
	 * @param a the first integer.
	 * @param b the second integer.
	 * @return the GCD.
	 */
	public static int gcd(int a, int b)
	{
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}
	
	/**
	 * Checks if an integer is a valid power of two.
	 * @param x the input value.
	 * @return true if it is, false if not.
	 */
	public static boolean isPowerOfTwo(int x)
	{
		return (x & (x-1)) == 0;
	}
	
	/**
	 * Checks if an integer is a valid power of two.
	 * @param x the input value.
	 * @return true if it is, false if not.
	 */
	public static boolean isPowerOfTwo(long x)
	{
		return (x & (x-1L)) != 0L;
	}
	
	/**
	 * Returns the percentage of an integer to the nearest complete integer.
	 * <br>Example: getPercent(20,50) returns 10.
	 * <br>Example 2: getPercent(10,25) returns 2.
	 * @param x the integer.
	 * @param percentage the percentage.
	 * @return the result equal to floor of x*(percentage/100), mathematically.
	 */
	public static int getPercent(int x, float percentage)
	{
		return (int)(x*(percentage/100f));
	}

	/**
	 * Converts radians to degrees.
	 * @param radians the input angle in radians.
	 * @return the resultant angle in degrees.
	 */
	public static double radToDeg(double radians)
	{
		return radians * (180/Math.PI);
	}

	/**
	 * Converts degrees to radians.
	 * @param degrees the input angle in degrees.
	 * @return the resultant angle in radians.
	 */
	public static double degToRad(double degrees)
	{
		return (degrees * Math.PI)/180;
	}

	/**
	 * Takes an angle in degrees and corrects it to the [0, 360) interval.
	 * @param angle the input angle.
	 * @return the equivalent angle in degrees.
	 */
	public static double sanitizeAngleDegrees(double angle)
	{
		if (angle >= 360.0)
			angle %= 360.0;
		else if (angle < 0.0)
			angle = (angle % 360.0) + 360.0;
		return angle;
	}

	/**
	 * Takes an angle in radians and corrects it to the [0, 2PI) interval.
	 * @param angle the input angle.
	 * @return the equivalent angle in radians.
	 */
	public static double sanitizeAngleRadians(double angle)
	{
		if (angle >= TWO_PI)
			angle %= TWO_PI;
		else if (angle < 0.0)
			angle = (angle % TWO_PI) + TWO_PI;
		return angle;
	}

	/**
	 * Rounds to the nearest increment.
	 * <br>Example: roundToNearest(3.4, 1.0) returns 3.0. 
	 * <br>Example: roundToNearest(3.4, 5.0) returns 5.0. 
	 * <br>Example: roundToNearest(3.4, 0.3) returns 3.3. 
	 * @param value the value to round.
	 * @param increment the incremental value. 
	 * @return the nearest increment using the input value.
	 */
	public static double roundToNearest(double value, double increment)
	{
		increment = Math.abs(increment);
		return Math.round(value / increment) * increment;
	}
	
	/**
	 * Coerces an integer to the range bounded by lo and hi.
	 * <br>Example: clampValue(32,-16,16) returns 16.
	 * <br>Example: clampValue(4,-16,16) returns 4.
	 * <br>Example: clampValue(-1000,-16,16) returns -16.
	 * @param val the integer.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "forced" into the range.
	 */
	public static int clampValue(int val, int lo, int hi)
	{
		return Math.min(Math.max(val,lo),hi);
	}

	/**
	 * Coerces a short to the range bounded by lo and hi.
	 * <br>Example: clampValue(32,-16,16) returns 16.
	 * <br>Example: clampValue(4,-16,16) returns 4.
	 * <br>Example: clampValue(-1000,-16,16) returns -16.
	 * @param val the short.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "forced" into the range.
	 */
	public static short clampValue(short val, short lo, short hi)
	{
		return (short)Math.min((short)Math.max(val,lo),hi);
	}

	/**
	 * Coerces a float to the range bounded by lo and hi.
	 * <br>Example: clampValue(32,-16,16) returns 16.
	 * <br>Example: clampValue(4,-16,16) returns 4.
	 * <br>Example: clampValue(-1000,-16,16) returns -16.
	 * @param val the float.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "forced" into the range.
	 */
	public static float clampValue(float val, float lo, float hi)
	{
		return Math.min(Math.max(val,lo),hi);
	}

	/**
	 * Coerces a double to the range bounded by lo and hi.
	 * <br>Example: clampValue(32,-16,16) returns 16.
	 * <br>Example: clampValue(4,-16,16) returns 4.
	 * <br>Example: clampValue(-1000,-16,16) returns -16.
	 * @param val the double.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "forced" into the range.
	 */
	public static double clampValue(double val, double lo, double hi)
	{
		return Math.min(Math.max(val,lo),hi);
	}
	
	
	/**
	 * Coerces an integer to the range bounded by lo and hi, by "wrapping" the value.
	 * <br>Example: wrapValue(32,-16,16) returns 0.
	 * <br>Example: wrapValue(4,-16,16) returns 4.
	 * <br>Example: wrapValue(-1000,-16,16) returns 8.
	 * @param val the integer.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "wrapped" into the range.
	 */
	public static int wrapValue(int val, int lo, int hi)
	{
		val = val - (int)(val - lo) / (hi - lo) * (hi - lo);
	   	if (val < 0)
	   		val = val + hi - lo;
	   	return val;
	}

	/**
	 * Coerces a short to the range bounded by lo and hi, by "wrapping" the value.
	 * <br>Example: wrapValue(32,-16,16) returns 0.
	 * <br>Example: wrapValue(4,-16,16) returns 4.
	 * <br>Example: wrapValue(-1000,-16,16) returns 8.
	 * @param val the short.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "wrapped" into the range.
	 */
	public static short wrapValue(short val, short lo, short hi)
	{
		val = (short)(val - (val - lo) / (hi - lo) * (hi - lo));
	   	if (val < 0)
	   		val = (short)(val + hi - lo);
	   	return val;
	}

	/**
	 * Coerces a float to the range bounded by lo and hi, by "wrapping" the value.
	 * <br>Example: wrapValue(32,-16,16) returns 0.
	 * <br>Example: wrapValue(4,-16,16) returns 4.
	 * <br>Example: wrapValue(-1000,-16,16) returns 8.
	 * @param val the float.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "wrapped" into the range.
	 */
	public static float wrapValue(float val, float lo, float hi)
	{
		float range = hi - lo;
		val = val - lo;
		val = (val % range);
		if (val < 0.0)
			val = val + hi;
		return val;
	}

	/**
	 * Coerces a double to the range bounded by lo and hi, by "wrapping" the value.
	 * <br>Example: wrapValue(32,-16,16) returns 0.
	 * <br>Example: wrapValue(4,-16,16) returns 4.
	 * <br>Example: wrapValue(-1000,-16,16) returns 8.
	 * @param val the double.
	 * @param lo the lower bound.
	 * @param hi the upper bound.
	 * @return the value after being "wrapped" into the range.
	 */
	public static double wrapValue(double val, double lo, double hi)
	{
		double range = hi - lo;
		val = val - lo;
		val = (val % range);
		if (val < 0.0)
			val = val + hi;
		return val;
	}

	/**
	 * Logically "and"-s two boolean arrays together.
	 * If both arrays are not the same size, an array of length <i>max(b1.length, b2.length)</i>
	 * is returned and the longer array is "and"-ed against falses past <i>min(b1.length, b2.length)</i>.
	 * @param b1 the first array.
	 * @param b2 the second array.
	 * @return A new boolean array that is the logical "and" of both arrays.
	 */
	public static boolean[] andBooleanArrays(boolean[] b1, boolean[] b2)
	{
		boolean[] longer = b1.length > b2.length ? b1 : b2;
		boolean[] shorter = b1 == longer ? b2 : b1;
		boolean[] out = new boolean[longer.length];
		if (longer.length != shorter.length)
		{
			boolean[] b = new boolean[longer.length];
			System.arraycopy(shorter, 0, b, 0, shorter.length);
			shorter = b;
		}
		
		for (int i = 0; i < longer.length; i++)
			out[i] = longer[i] && shorter[i];
	
		return out;
	}

	/**
	 * Logically "or"-s two boolean arrays together.
	 * If both arrays are not the same size, an array of length <i>max(b1.length, b2.length)</i>
	 * is returned and the longer array is "or"-ed against falses past <i>min(b1.length, b2.length)</i>.
	 * @param b1 the first array.
	 * @param b2 the second array.
	 * @return A new boolean array that is the logical "or" of both arrays.
	 */
	public static boolean[] orBooleanArrays(boolean[] b1, boolean[] b2)
	{
		boolean[] longer = b1.length > b2.length ? b1 : b2;
		boolean[] shorter = b1 == longer ? b2 : b1;
		boolean[] out = new boolean[longer.length];
		if (longer.length != shorter.length)
		{
			boolean[] b = new boolean[longer.length];
			System.arraycopy(shorter, 0, b, 0, shorter.length);
			shorter = b;
		}
		
		for (int i = 0; i < longer.length; i++)
			out[i] = longer[i] || shorter[i];
	
		return out;
	}

	/**
	 * Logically "xor"-s two boolean arrays together.
	 * If both arrays are not the same size, an array of length <i>max(b1.length, b2.length)</i>
	 * is returned and the longer array is "xor"-ed against falses past <i>min(b1.length, b2.length)</i>.
	 * @param b1 the first array.
	 * @param b2 the second array.
	 * @return A new boolean array that is the logical "xor" of both arrays.
	 */
	public static boolean[] xorBooleanArrays(boolean[] b1, boolean[] b2)
	{
		boolean[] longer = b1.length > b2.length ? b1 : b2;
		boolean[] shorter = b1 == longer ? b2 : b1;
		boolean[] out = new boolean[longer.length];
		if (longer.length != shorter.length)
		{
			boolean[] b = new boolean[longer.length];
			System.arraycopy(shorter, 0, b, 0, shorter.length);
			shorter = b;
		}
		
		for (int i = 0; i < longer.length; i++)
			out[i] = longer[i] ^ shorter[i];
	
		return out;
	}

	/**
	 * Returns the value that "value" is closest to.
	 * @param value the input value
	 * @param v1 first evaluating value
	 * @param v2 second evaluating value
	 * @return either v1 or v2, whichever's closer.
	 */
	public static int closer(double value, int v1, int v2)
	{
		return (Math.abs(value-v1) <= Math.abs(value-v2)) ? v1 : v2;
	}

	/**
	 * Returns (val - min) if val is closer to min than max, (max - val) otherwise.
	 * Result is always positive.
	 * @param val the value to test.
	 * @param min the minimum bound.
	 * @param max the maximum bound.
	 * @return the closer component value.
	 */
	public static double closerComponent(double val, double min, double max)
	{
		return Math.abs(val - min) < Math.abs(val - max) ? (val - min) : (max - val);
	}

	/**
	 * Returns the value that "value" is farthest from.
	 * @param value the input value
	 * @param v1 first evaluating value
	 * @param v2 second evaluating value
	 * @return either v1 or v2, whichever's farther.
	 */
	public static int farther(double value, int v1, int v2)
	{
		return (Math.abs(value-v1) >= Math.abs(value-v2)) ? v1 : v2;
	}

	/**
	 * Gives a value that is the result of a linear interpolation between two values.
	 * @param factor the interpolation factor.
	 * @param x the first value.
	 * @param y the second value.
	 * @return the interpolated value.
	 */
	public static double linearInterpolate(double factor, double x, double y)
	{
		return factor * (y - x) + x;
	}
	
	/**
	 * Gives a value that is the result of a cosine interpolation between two values.
	 * @param factor the interpolation factor.
	 * @param x the first value.
	 * @param y the second value.
	 * @return the interpolated value.
	 */
	public static double cosineInterpolate(double factor, double x, double y)
	{
		double ft = factor * Math.PI;
		double f = (1 - Math.cos(ft)) * .5;
		return f * (y - x) + x;
	}
	
	/**
	 * Gives a value that is the result of a cublic interpolation between two values.
	 * Requires two outside values to predict a curve more accurately.
	 * @param factor the interpolation factor between x and y.
	 * @param w the value before the first.
	 * @param x the first value.
	 * @param y the second value.
	 * @param z the value after the second.
	 * @return the interpolated value.
	 */
	public static double cubicInterpolate(double factor, double w, double x, double y, double z)
	{
		double p = (z - y) - (w - x);
		double q = (w - x) - p;
		double r = y - w;
		double s = x;
		return (p*factor*factor*factor) + (q*factor*factor) + (r*factor) + s;
	}
	
	/**
	 * Special angle interpolation. 
	 * Best used for presentation, not for calculation.
	 * @param factor interpolation factor.
	 * @param degA the first angle in degrees.
	 * @param degB the second angle in degrees.
	 * @return the resultant angle.
	 */
	public static double angleInterpolateDegrees(double factor, double degA, double degB)
	{
		double max = Math.max(degA, degB);
		double min = Math.min(degA, degB);
		
		if (max - min > 180.0)
		{
			if (degA > degB)
				degA = degA - 360.0;
			else if (degB > degA)
				degB = degB - 360.0;
		}
		
		return factor * (degB - degA) + degA;		
	}
	
	/**
	 * Special angle interpolation. 
	 * Best used for presentation, not for calculation.
	 * @param factor interpolation factor.
	 * @param radA the first angle in degrees.
	 * @param radB the second angle in degrees.
	 * @return the resultant angle.
	 */
	public static double angleInterpolateRadians(double factor, double radA, double radB)
	{
		double max = Math.max(radA, radB);
		double min = Math.min(radA, radB);
		
		if (max - min > Math.PI)
		{
			if (radA > radB)
				radA = radA - Math.PI*2;
			else if (radB > radA)
				radB = radB - Math.PI*2;
		}
		
		return factor * (radB - radA) + radA;		
	}
	
	/**
	 * Corrects a set of view clipping bounds to fit into a different aspect.
	 * @param targetAspect the target aspect.
	 * @param left the leftmost clipping bound.
	 * @param right the rightmost clipping bound.
	 * @param bottom the bottom clipping bound.
	 * @param top the top clipping bound.
	 * @param outBounds the output array to put the calculated planes.
	 */
	public static void correctClippingAspect(double targetAspect, double left, double right, double bottom, double top, double[] outBounds)
	{
		double viewWidth = Math.max(left, right) - Math.min(left, right);
		double viewHeight = Math.max(bottom, top) - Math.min(bottom, top);
		double viewAspect = viewWidth / viewHeight;
		
		if (targetAspect >= viewAspect)
		{
			double axis = targetAspect * viewHeight;
			double widthDiff = (axis - viewWidth) / 2f;
			right = left + viewWidth + widthDiff;
			left = left - widthDiff;
		}
		else
		{
			double axis = (1.0f / targetAspect) * viewWidth;
			double heightDiff = (axis - viewHeight) / 2f;
			top = bottom + viewHeight + heightDiff;
			bottom = bottom - heightDiff;
		}
		
		outBounds[0] = left;
		outBounds[1] = right;
		outBounds[2] = bottom;
		outBounds[3] = top;
	}
	
	/**
	 * Gets a scalar factor that equals how "far along" a value is along an interval.
	 * @param value the value to test.
	 * @param lo the lower value of the interval.
	 * @param hi the higher value of the interval.
	 * @return a value between 0 and 1 describing this distance 
	 * 		(0 = beginning or less, 1 = end or greater), or 0 if lo and hi are equal.
	 */
	public static double getInterpolationFactor(double value, double lo, double hi)
	{
		if (lo == hi)
			return 0.0;
		return clampValue((value - lo) / (hi - lo), 0, 1);
	}
	
	/**
	 * Returns the angular rotation of a vector described in two dimensions.
	 * Result is in degrees.
	 * @param x the x-component.
	 * @param y the y-component.
	 * @return a number in the range [0, 360). 0 is considered to be EAST. 
	 */
	public static double getVectorAngleDegrees(double x, double y)
	{
		if (x != 0.0)
		{
			double deg = sanitizeAngleDegrees(radToDeg(Math.atan(y / x)));
			return deg + (x < 0 ? 180 : 0);
		}
		else
		{
			return (y < 0 ? 270d : 90d);
		}
	}
	
	/**
	 * Returns the angular rotation of a vector described in two dimensions.
	 * Result is in radians.
	 * @param x the x-component.
	 * @param y the y-component.
	 * @return a number in the range [0, 2*PI). 0 is considered to be EAST. 
	 */
	public static double getVectorAngleRadians(double x, double y)
	{
		if (x != 0.0)
		{
			double rad = sanitizeAngleRadians(Math.atan(y / x));
			return rad + (x < 0 ? Math.PI : 0);
		}
		else
		{
			return (y < 0 ? THREE_PI_OVER_TWO : PI_OVER_TWO);
		}
	}
	
	/**
	 * Returns the length of a vector by its components.
	 * @param x the x-component.
	 * @param y the y-component.
	 * @return the length of the vector.
	 */
	public static double getVectorLength(double x, double y)
	{
		return Math.sqrt(getVectorLengthSquared(x, y));
	}

	/**
	 * Returns the squared length of a vector by its components.
	 * @param x the x-component.
	 * @param y the y-component.
	 * @return the length of the vector.
	 */
	public static double getVectorLengthSquared(double x, double y)
	{
		return x*x + y*y;
	}

	/**
	 * Returns the length of a vector by its components.
	 * @param x the x-component.
	 * @param y the y-component.
	 * @param z the z-component.
	 * @return the length of the vector.
	 */
	public static double getVectorLength(double x, double y, double z)
	{
		return Math.sqrt(getVectorLengthSquared(x, y, z));
	}

	/**
	 * Returns the squared length of a vector by its components.
	 * @param x the x-component.
	 * @param y the y-component.
	 * @param z the z-component.
	 * @return the length of the vector.
	 */
	public static double getVectorLengthSquared(double x, double y, double z)
	{
		return x*x + y*y + z*z;
	}

	/**
	 * Returns the dot product of two vectors.
	 * @param v1x the first vector's x-component.
	 * @param v1y the first vector's y-component.
	 * @param v2x the second vector's x-component.
	 * @param v2y the second vector's y-component.
	 * @return the dot product of both vectors.
	 */
	public static double getVectorDotProduct(double v1x, double v1y, double v2x, double v2y)
	{
		return v1x * v2x + v1y * v2y;
	}

	/**
	 * Returns the dot product of two vectors.
	 * @param v1x the first vector's x-component.
	 * @param v1y the first vector's y-component.
	 * @param v1z the first vector's z-component.
	 * @param v2x the second vector's x-component.
	 * @param v2y the second vector's y-component.
	 * @param v2z the second vector's z-component.
	 * @return the dot product of both vectors.
	 */
	public static double getVectorDotProduct(double v1x, double v1y, double v1z, double v2x, double v2y, double v2z)
	{
		return v1x * v2x + v1y * v2y + v1z * v2z;
	}

	/**
	 * Returns the dot product of two vectors, converted to unit vectors first.
	 * NOTE: Zero vectors will cause a <b>divide by zero</b>!
	 * @param v1x the first vector's x-component.
	 * @param v1y the first vector's y-component.
	 * @param v2x the second vector's x-component.
	 * @param v2y the second vector's y-component.
	 * @return the dot product of both vectors.
	 */
	public static double getVectorUnitDotProduct(double v1x, double v1y, double v2x, double v2y)
	{
		double v1d = getVectorLength(v1x, v1y); 
		double v2d = getVectorLength(v2x, v2y);
		v1x = v1x / v1d;
		v1y = v1y / v1d;
		v2x = v2x / v2d;
		v2y = v2y / v2d;
		return getVectorDotProduct(v1x, v1y, v2x, v2y);
	}

	/**
	 * Returns the dot product of two vectors, converted to unit vectors first.
	 * NOTE: Zero vectors will cause a <b>divide by zero</b>!
	 * @param v1x the first vector's x-component.
	 * @param v1y the first vector's y-component.
	 * @param v1z the first vector's z-component.
	 * @param v2x the second vector's x-component.
	 * @param v2y the second vector's y-component.
	 * @param v2z the second vector's z-component.
	 * @return the dot product of both vectors.
	 */
	public static double getVectorUnitDotProduct(double v1x, double v1y, double v1z, double v2x, double v2y, double v2z)
	{
		double v1d = getVectorLength(v1x, v1y, v1z); 
		double v2d = getVectorLength(v2x, v2y, v2z);
		v1x = v1x / v1d;
		v1y = v1y / v1d;
		v1z = v1z / v1d;
		v2x = v2x / v2d;
		v2y = v2y / v2d;
		v2z = v2z / v2d;
		return getVectorDotProduct(v1x, v1y, v1z, v2x, v2y, v2z);
	}

	/**
	 * Returns a simulated dot product between two radian angles
	 * as though they were unit vectors, rotated.
	 * @param radA the first angle in radians.
	 * @param radB the second angle in radians.
	 * @return the resultant dot product.
	 */
	public static double getAngleDotProductRadians(double radA, double radB)
	{
		double v1x = Math.cos(radA);
		double v1y = Math.sin(radA);
		double v2x = Math.cos(radB);
		double v2y = Math.sin(radB);
		return getVectorDotProduct(v1x, v1y, v2x, v2y);
	}

	/**
	 * Returns a simulated dot product between two degree angles
	 * as though they were unit vectors, rotated.
	 * @param degA the first angle in degrees.
	 * @param degB the second angle in degrees.
	 * @return the resultant dot product.
	 */
	public static double getAngleDotProductDegrees(double degA, double degB)
	{
		return getAngleDotProductRadians(degToRad(degA), degToRad(degB));
	}

	/**
	 * Returns the length of a line by 
	 * the coordinates of the two points that comprise it.
	 * @param x0 the first point's x-component.
	 * @param y0 the first point's y-component.
	 * @param x1 the second point's x-component.
	 * @param y1 the second point's y-component.
	 * @return the length of the line.
	 */
	public static double getLineLength(double x0, double y0, double x1, double y1)
	{
		return Math.sqrt(getLineLengthSquared(x0, y0, x1, y1));
	}

	/**
	 * Returns the squared length of a line by 
	 * the coordinates of the two points that comprise it.
	 * @param x0 the first point's x-component.
	 * @param y0 the first point's y-component.
	 * @param x1 the second point's x-component.
	 * @param y1 the second point's y-component.
	 * @return the length of the line.
	 */
	public static double getLineLengthSquared(double x0, double y0, double x1, double y1)
	{
		return getVectorLengthSquared(x1 - x0, y1 - y0);
	}

	/**
	 * Returns the length of a line by 
	 * the coordinates of the two points that comprise it.
	 * @param x0 the first point's x-component.
	 * @param y0 the first point's y-component.
	 * @param z0 the first point's z-component.
	 * @param x1 the second point's x-component.
	 * @param y1 the second point's y-component.
	 * @param z1 the second point's z-component.
	 * @return the length of the line.
	 */
	public static double getLineLength(double x0, double y0, double z0, double x1, double y1, double z1)
	{
		return Math.sqrt(getLineLengthSquared(x0, y0, z0, x1, y1, z1));
	}

	/**
	 * Returns the squared length of a line by 
	 * the coordinates of the two points that comprise it.
	 * @param x0 the first point's x-component.
	 * @param y0 the first point's y-component.
	 * @param z0 the first point's z-component.
	 * @param x1 the second point's x-component.
	 * @param y1 the second point's y-component.
	 * @param z1 the second point's z-component.
	 * @return the length of the line.
	 */
	public static double getLineLengthSquared(double x0, double y0, double z0, double x1, double y1, double z1)
	{
		return getVectorLengthSquared(x1 - x0, y1 - y0, z1 - z0);
	}

	/**
	 * Returns the difference in degrees between two angles.
	 * @param angle1 the first angle in DEGREES.
	 * @param angle2 the second angle in DEGREES.
	 * @return a number in the range [0, 180]. 0 is an EXACT match. 
	 */
	public static double getAngularDistanceDegrees(double angle1, double angle2)
	{
		return Math.abs(getRelativeAngleDegrees(angle1, angle2)); 
	}
	
	/**
	 * Returns the difference in radians between two angles.
	 * @param angle1 the first angle in RADIANS.
	 * @param angle2 the second angle in RADIANS.
	 * @return a number in the range [0, {@link Math#PI}]. 0 is an EXACT match. 
	 */
	public static double getAngularDistanceRadians(double angle1, double angle2)
	{
		return Math.abs(getRelativeAngleRadians(angle1, angle2)); 
	}

	/**
	 * Returns the relative angle between two angles.
	 * @param angle1 the first angle in DEGREES.
	 * @param angle2 the second angle in DEGREES.
	 * @return a number in the range [-180, 180]. 0 is an EXACT match.
	 */
	public static double getRelativeAngleDegrees(double angle1, double angle2)
	{
		return ((((angle1 - angle2) % 360.0) + 540.0) % 360.0) - 180.0;
	}
	
	/**
	 * Returns the relative angle between two angles.
	 * @param angle1 the first angle in RADIANS.
	 * @param angle2 the second angle in RADIANS.
	 * @return a number in the range [-Math.PI, Math.PI]. 0 is an EXACT match.
	 */
	public static double getRelativeAngleRadians(double angle1, double angle2)
	{
		return ((((angle1 - angle2) % TWO_PI) + (3 * Math.PI)) % TWO_PI) - Math.PI;
	}

	/**
	 * Returns the observed angle of an object in space facing a direction. 
	 * @param viewX the view origin X-coordinate.
	 * @param viewY the view origin Y-coordinate.
	 * @param targetX the target origin X-coordinate.
	 * @param targetY the target origin Y-coordinate.
	 * @param targetDegrees the target facing degrees. 0 is EAST, 90 is NORTH...
	 * @return the observed absolute angle (degrees) from the view. 
	 */
	public static double getObservedAngleDegrees(double viewX, double viewY, double targetX, double targetY, double targetDegrees)
	{
		double signedDegrees = -targetDegrees + radToDeg(Math.atan2(targetX - viewX, targetY - viewY));
		return signedDegrees < 0 ? signedDegrees + 360.0 : signedDegrees;
	}
	
	/**
	 * Returns the observed angle of an object in space facing a direction. 
	 * @param viewX the view origin X-coordinate.
	 * @param viewY the view origin Y-coordinate.
	 * @param targetX the target origin X-coordinate.
	 * @param targetY the target origin Y-coordinate.
	 * @param targetRadians the target facing radians. 0 is EAST, PI/2 is NORTH...
	 * @return the observed absolute angle (radians) from the view. 
	 */
	public static double getObservedAngleRadians(double viewX, double viewY, double targetX, double targetY, double targetRadians)
	{
		double signedRads = -targetRadians + Math.atan2(targetX - viewX, targetY - viewY);
		return signedRads < 0 ? signedRads + TWO_PI : signedRads;
	}

	/**
	 * Returns the signed area of a triangular area made up of 3 points.
	 * @param ax the first point, x-coordinate.
	 * @param ay the first point, y-coordinate.
	 * @param bx the second point, x-coordinate.
	 * @param by the second point, y-coordinate.
	 * @param cx the third point, x-coordinate.
	 * @param cy the third point, y-coordinate.
	 * @return the calculated area.
	 */
	public static double getTriangleArea(double ax, double ay, double bx, double by, double cx, double cy)
	{
		return Math.abs(getTriangleAreaSigned(ax, ay, bx, by, cx, cy));
	}

	/**
	 * Returns the signed area of a triangular area made up of 3 points.
	 * @param ax the first point, x-coordinate.
	 * @param ay the first point, y-coordinate.
	 * @param bx the second point, x-coordinate.
	 * @param by the second point, y-coordinate.
	 * @param cx the third point, x-coordinate.
	 * @param cy the third point, y-coordinate.
	 * @return the calculated area.
	 */
	public static double getTriangleAreaSigned(double ax, double ay, double bx, double by, double cx, double cy)
	{
		return getTriangleAreaDoubleSigned(ax, ay, bx, by, cx, cy) / 2.0;
	}

	/**
	 * Returns the doubled signed area of a triangular area made up of 3 points.  
	 * @param ax the first point, x-coordinate.
	 * @param ay the first point, y-coordinate.
	 * @param bx the second point, x-coordinate.
	 * @param by the second point, y-coordinate.
	 * @param cx the third point, x-coordinate.
	 * @param cy the third point, y-coordinate.
	 * @return the calculated area.
	 */
	public static double getTriangleAreaDoubleSigned(double ax, double ay, double bx, double by, double cx, double cy)
	{
		return (ax - cx) * (by - cy) - (ay - cy) * (bx - cx);
	}

	/**
	 * Checks what "side" a point is on a line.
	 * Line orientation is "point A looking at point B"
	 * @param ax the line segment, first point, x-coordinate.
	 * @param ay the line segment, first point, y-coordinate.
	 * @param bx the line segment, second point, x-coordinate.
	 * @param by the line segment, second point, y-coordinate.
	 * @param px the point, x-coordinate.
	 * @param py the point, y-coordinate.
	 * @return a scalar value representing the line side (<code>0</code> is on the line, <code>&lt; 0</code> is left, <code>&gt; 0</code> is right).
	 */
	public static double getLinePointSide(double ax, double ay, double bx, double by, double px, double py)
	{
		double nax = (by - ay);
		double nay = -(bx - ax);
		return getVectorDotProduct(nax, nay, px - ax, py - ay);
	}

	/**
	 * Tests if an intersection occurs between two line segments.
	 * @param ax the first line segment, first point, x-coordinate.
	 * @param ay the first line segment, first point, y-coordinate.
	 * @param bx the first line segment, second point, x-coordinate.
	 * @param by the first line segment, second point, y-coordinate.
	 * @param cx the second line segment, first point, x-coordinate.
	 * @param cy the second line segment, first point, y-coordinate.
	 * @param dx the second line segment, second point, x-coordinate.
	 * @param dy the second line segment, second point, y-coordinate.
	 * @return a scalar value representing how far along the first line segment the intersection occurred, or {@link Double#NaN} if no intersection.
	 */
	public static double getIntersectionLine(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy)
	{
		double a1 = getTriangleAreaDoubleSigned(ax, ay, bx, by, dx, dy);
		double a2 = getTriangleAreaDoubleSigned(ax, ay, bx, by, cx, cy);
		
		// If the triangle areas have opposite signs. 
		if (a1 != 0.0 && a2 != 0.0 && a1 * a2 < 0.0)
		{
			double a3 = getTriangleAreaDoubleSigned(cx, cy, dx, dy, ax, ay);
			double a4 = a3 + a2 - a1;
			
			if (a3 * a4 < 0.0)
			{
				return a3 / (a3 - a4);
			}
		}
		
		return Double.NaN;
	}

	/**
	 * Tests if a line intersects with a plane.
	 * @param ax the line's first point, x-coordinate.
	 * @param ay the line's first point, y-coordinate.
	 * @param az the line's first point, z-coordinate.
	 * @param bx the line's second point, x-coordinate.
	 * @param by the line's second point, y-coordinate.
	 * @param bz the line's second point, z-coordinate.
	 * @param pnx the unit-vector plane normal, x-component.
	 * @param pny the unit-vector plane normal, y-component.
	 * @param pnz the unit-vector plane normal, z-component.
	 * @param pdist the distance of the plane from the origin (dot product of normal and a point on the plane).
	 * @return a scalar value representing how far along the line segment the intersection occurred, or {@link Double#NaN} if no intersection.
	 */
	public static double getIntersectionLinePlane(double ax, double ay, double az, double bx, double by, double bz, double pnx, double pny, double pnz, double pdist)
	{
		double vx = bx - ax;
		double vy = by - ay;
		double vz = bz - az;
		
		double dotnormline = getVectorDotProduct(pnx, pny, pnz, vx, vy, vz);
		if (dotnormline == 0.0)
			return Double.NaN;
		
		double t = pdist - getVectorDotProduct(pnx, pny, pnz, ax, ay, az) / dotnormline;
		
		return t >= 0.0 && t <= 1.0 ? t : Double.NaN;
	}

	/**
	 * Tests if a line segment intersects with a circle.
	 * @param ax the line segment, first point, x-coordinate.
	 * @param ay the line segment, first point, y-coordinate.
	 * @param bx the line segment, second point, x-coordinate.
	 * @param by the line segment, second point, y-coordinate.
	 * @param ccx the circle center, x-coordinate.
	 * @param ccy the circle center, y-coordinate.
	 * @param crad the circle radius.
	 * @return a scalar value representing how far along the line segment the intersection occurred, or {@link Double#NaN} if no intersection.
	 */
	public static double getIntersectionLineCircle(double ax, double ay, double bx, double by, double ccx, double ccy, double crad)
	{
		// set up vector.
		double mx = ax - ccx;
		double my = ay - ccy;
		
		double linelen = getLineLength(ax, ay, bx, by);
		
		// normalize line segment
		double dx = (bx - ax) / linelen; 
		double dy = (by - ay) / linelen;
		
		double b = getVectorDotProduct(mx, my, dx, dy);
		double c = getVectorDotProduct(mx, my, mx, my) - (crad * crad);
		
		// Exit if r's origin outside s (c > 0) and r pointing away from s (b > 0)
		if (c > 0.0 && b > 0.0)
			return Double.NaN;
		
		double discr = (b * b) - c;
		
		// Negative discriminant = ray misses circle.
		if (discr < 0.0)
			return Double.NaN;

		// Compute smallest distance on ray in intersection.
		double out = -b - Math.sqrt(discr);

		// if out > segment length, not intersecting.
		if (out > linelen)
			return Double.NaN;
		
		// out = negative = inside circle.
		if (out < 0.0)
			return 0.0;
		
		return out / linelen;
	}

	/**
	 * Tests if a line segment intersects with a box.
	 * @param ax the line segment, first point, x-coordinate.
	 * @param ay the line segment, first point, y-coordinate.
	 * @param bx the line segment, second point, x-coordinate.
	 * @param by the line segment, second point, y-coordinate.
	 * @param bcx the box center, x-coordinate.
	 * @param bcy the box center, y-coordinate.
	 * @param bhw the box half width.
	 * @param bhh the box half height.
	 * @return a scalar value representing how far along the line segment the intersection occurred, or {@link Double#NaN} if no intersection.
	 */
	public static double getIntersectionLineBox(double ax, double ay, double bx, double by, double bcx, double bcy, double bhw, double bhh)
	{
		double linelen = getLineLength(ax, ay, bx, by);
		double tmin = 0.0;
		double tmax = linelen;
		
		// normalize line segment
		double dx = (bx - ax) / linelen; 
		double dy = (by - ay) / linelen;

		// x-slab
		if (Math.abs(dx) == 0.0)
		{
			// Ray is parallel to slab - check if origin is inside.
			if (ax < bcx - bhw || ax > bcx + bhw)
				return Double.NaN;
		}
		else
		{
			// Compute intersection t value with near and far plane.
			double ood = 1.0 / dx;
			double t1 = (bcx - bhw - ax) * ood;
			double t2 = (bcx + bhw - ax) * ood;
			tmin = Math.max(tmin, Math.min(t1,  t2));
			tmax = Math.min(tmax, Math.max(t1,  t2));
			// Exit with no collision?
			if (tmin > tmax)
				return Double.NaN;
		}
		
		// y-slab
		if (Math.abs(dy) == 0.0)
		{
			// Ray is parallel to slab - check if origin is inside.
			if (ay < bcy - bhh || ay > bcy + bhh)
				return Double.NaN;
		}
		else
		{
			// Compute intersection t value with near and far plane.
			double ood = 1.0 / dy;
			double t1 = (bcy - bhh - ay) * ood;
			double t2 = (bcy + bhh - ay) * ood;
			tmin = Math.max(tmin, Math.min(t1,  t2));
			tmax = Math.min(tmax, Math.max(t1,  t2));
			// Exit with no collision?
			if (tmin > tmax)
				return Double.NaN;
		}
		
		return tmin / linelen;
	}
	
	/**
	 * Returns if two described circles intersect.  
	 * @param spx the first circle center, x-coordinate.
	 * @param spy the first circle center, y-coordinate.
	 * @param srad the first circle radius.
	 * @param tpx the second circle center, x-coordinate.
	 * @param tpy the second circle center, y-coordinate.
	 * @param trad the second circle radius.
	 * @return true if so, false if not.
	 */
	public static boolean getIntersectionCircle(double spx, double spy, double srad, double tpx, double tpy, double trad)
	{
		return getLineLength(spx, spy, tpx, tpy) < srad + trad;
	}

	/**
	 * Returns if a circle and box intersect.  
	 * @param ccx the circle center, x-coordinate.
	 * @param ccy the circle center, y-coordinate.
	 * @param crad the circle radius.
	 * @param bcx the box center, x-coordinate.
	 * @param bcy the box center, y-coordinate.
	 * @param bhw the box half width.
	 * @param bhh the box half height.
	 * @return if an intersection occurred.
	 */
	public static boolean getIntersectionCircleBox(double ccx, double ccy, double crad, double bcx, double bcy, double bhw, double bhh)
	{
		double tx0 = bcx - bhw;
		double tx1 = bcx + bhw;
		double ty0 = bcy - bhh;
		double ty1 = bcy + bhh;
	
		// Voronoi Region Test.
		if (ccx < tx0)
		{
			if (ccy < ty0)
				return getLineLength(ccx, ccy, tx0, ty0) < crad;
			else if (ccy > ty1)
				return getLineLength(ccx, ccy, tx0, ty1) < crad;
			else
				return getLineLength(ccx, ccy, tx0, ccy) < crad;
		}
		else if (ccx > tx1)
		{
			if (ccy < ty0)
				return getLineLength(ccx, ccy, tx1, ty0) < crad;
			else if (ccy > ty1)
				return getLineLength(ccx, ccy, tx1, ty1) < crad;
			else
				return getLineLength(ccx, ccy, tx1, ccy) < crad;
		}
		else
		{
			if (ccy < ty0)
				return getLineLength(ccx, ccy, ccx, ty0) < crad;
			else if (ccy > ty1)
				return getLineLength(ccx, ccy, ccx, ty1) < crad;
			else // circle center is inside box
				return true;
		}
	
	}

	/**
	 * Tests if two boxes intersect.  
	 * @param spx the first box center, x-coordinate.
	 * @param spy the first box center, y-coordinate.
	 * @param shw the first box half width.
	 * @param shh the first box half height.
	 * @param tpx the second box center, x-coordinate.
	 * @param tpy the second box center, y-coordinate.
	 * @param thw the second box half width.
	 * @param thh the second box half height.
	 * @return true if an intersection occurred, false if not.
	 */
	public static boolean getIntersectionBox(double spx, double spy, double shw, double shh, double tpx, double tpy, double thw, double thh)
	{
		if (spx < tpx) // box to the left.
		{
			if (spx + shw < tpx - thw)
				return false;
			
			if (spy < tpy) // box to the bottom.
			{
				if (spy + shh < tpy - thh)
					return false;
				
				return true;
			}
			else // box to the top.
			{
				if (spy - shh > tpy + thh)
					return false;
				
				return true;
			}
		}
		else // box to the right
		{
			if (spx - shw > tpx + thw)
				return false;
	
			if (spy < tpy) // box to the bottom.
			{
				if (spy + shh < tpy - thh)
					return false;
				
				return true;
			}
			else // box to the top.
			{
				if (spy - shh > tpy + thh)
					return false;
				
				return true;
			}
		}
	}

	/**
	 * Checks the collision of two cylinders.
	 * @param sx the first cylinder, origin x-coordinate.
	 * @param sy the first cylinder, origin y-coordinate.
	 * @param sz the first cylinder, origin z-coordinate (base/floor of cylinder).
	 * @param srad the first cylinder radius.
	 * @param sh the first cylinder height.
	 * @param tx the second cylinder, origin x-coordinate.
	 * @param ty the second cylinder, origin y-coordinate.
	 * @param tz the second cylinder, origin z-coordinate (base/floor of cylinder).
	 * @param trad the second cylinder radius.
	 * @param th the second cylinder height.
	 * @return true if an intersection occurred, false if not.
	 */
	public static boolean getIntersectionCylinder(
		float sx, float sy, float sz, float srad, float sh,
		float tx, float ty, float tz, float trad, float th
	)
	{
		if (MathUtils.getIntersectionCircle(sx, sy, srad, tx, ty, trad))
		{
			float szh = sz + sh;
			float tzh = tz + th;
			return sz <= tzh && szh >= tz;
		}
		return false;
	}

	// Single "behind plane" consistent check.
	private static boolean isPointBehindPlane(double ax, double ay, double bx, double by, double px, double py)
	{
		return getLinePointSide(ax, ay, bx, by, px, py) < 0;
	}

	/**
	 * Checks if a point lies behind a 2D plane, represented by a line.
	 * Directionality of the line affects the plane - plane normal goes away from the "left" side of the line.
	 * @param ax the line segment, first point, x-coordinate.
	 * @param ay the line segment, first point, y-coordinate.
	 * @param bx the line segment, second point, x-coordinate.
	 * @param by the line segment, second point, y-coordinate.
	 * @param px the point, x-coordinate.
	 * @param py the point, y-coordinate.
	 * @return true if the point is past the plane, false if not.
	 * @see #getLinePointSide(double, double, double, double, double, double)
	 */
	public static boolean getIntersectionPoint2DPlane(double ax, double ay, double bx, double by, double px, double py)
	{
		return isPointBehindPlane(ax, ay, bx, by, px, py);
	}

	/**
	 * Checks if a circle breaks a 2D plane, represented by a line.
	 * Directionality of the line affects the plane - plane normal goes away from the "left" side of the line.
	 * @param ax the line segment, first point, x-coordinate.
	 * @param ay the line segment, first point, y-coordinate.
	 * @param bx the line segment, second point, x-coordinate.
	 * @param by the line segment, second point, y-coordinate.
	 * @param ccx the circle centerpoint, x-coordinate.
	 * @param ccy the circle centerpoint, y-coordinate.
	 * @param crad the circle centerpoint, radius.
	 * @return true if any part of the circle is past the plane, false if not.
	 * @see #getLinePointSide(double, double, double, double, double, double)
	 */
	public static boolean getIntersectionCircle2DPlane(double ax, double ay, double bx, double by, double ccx, double ccy, double crad)
	{
		// normal
		double vx = -(by - ay);
		double vy = bx - ax;
		double vlen = getVectorLength(vx, vy);
		// normalize to radius length.
		vx = vx / vlen * crad;
		vy = vy / vlen * crad;
		
		return isPointBehindPlane(ax, ay, bx, by, ccx + vx, ccy + vy) || isPointBehindPlane(ax, ay, bx, by, ccx - vx, ccy - vy);
	}

	/**
	 * Checks if an axis-aligned box breaks a 2D plane, represented by a line.
	 * Directionality of the line affects the plane - plane normal goes away from the "right" side of the line.
	 * @param ax the line segment, first point, x-coordinate.
	 * @param ay the line segment, first point, y-coordinate.
	 * @param bx the line segment, second point, x-coordinate.
	 * @param by the line segment, second point, y-coordinate.
	 * @param bcx the box center, x-coordinate.
	 * @param bcy the box center, y-coordinate.
	 * @param bhw the box half width.
	 * @param bhh the box half height.
	 * @return true if any part of the box is past the plane, false if not.
	 * @see #getLinePointSide(double, double, double, double, double, double)
	 */
	public static boolean getIntersectionBox2DPlane(double ax, double ay, double bx, double by, double bcx, double bcy, double bhw, double bhh)
	{
		return 
			isPointBehindPlane(ax, ay, bx, by, bcx + bhw, bcy + bhh) 
			|| isPointBehindPlane(ax, ay, bx, by, bcx - bhw, bcy + bhh)
			|| isPointBehindPlane(ax, ay, bx, by, bcx + bhw, bcy - bhh)
			|| isPointBehindPlane(ax, ay, bx, by, bcx - bhw, bcy - bhh)
		;
	}	
}
