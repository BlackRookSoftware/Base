package com.blackrook.base;

import java.io.IOException;

public final class SingleInstanceLockTest 
{
	public static void main(String[] args) throws IOException
	{
		try (SingleInstanceLock lock = new SingleInstanceLock("fartslol"))
		{
			System.out.println();
		} 
	}

}
