package com.blackrook.base.util;

public final class ArrayTest
{
	public static void main(String[] args)
	{
		Integer[] ints = { 1, 2, 3, 4, 5 };
		ArrayUtils.arrayRemove(ints, 4, null);
		ArrayUtils.arrayRemove(ints, 2, null);
		ArrayUtils.arrayRemove(ints, 0, null);
		ArrayUtils.arrayRemove(ints, 3, null);
		System.out.print("");
	}
}
