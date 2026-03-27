package com.blackrook.base;

import java.io.IOException;

public final class SingleInstanceTempFileLockTest 
{
	public static void main(String[] args) throws IOException
	{
		try (SingleInstanceTempFileLock lock = new SingleInstanceTempFileLock("fartslol"))
		{
			System.out.println();
		} 
	}

}
