package com.blackrook.base.util;

public final class MathTest 
{
	public static void main(String[] args)
	{
		System.out.println(MathUtils.fixedToDouble(16, 32 << 16));
		System.out.println(MathUtils.fixedToDouble(16, 1 << 15));
		System.out.println(MathUtils.doubleToFixed(16, 32.0));
		System.out.println(MathUtils.doubleToFixed(16, 0.5));
	}
}
