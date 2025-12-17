package com.blackrook.base.util;

import java.util.Arrays;
import java.util.Random;

public final class RandomTest 
{
	public static void main(String[] args)
	{
		Random random = new Random();
		Integer[] arr = {1,2,3,4,5};
		Integer[] out = RandomUtils.shuffleCopy(random, arr);
		System.out.println(Arrays.toString(out));
	}
}
