package com.blackrook.base;

/**
 * @author Matthew Tropiano
 */
public class SeriesExecutorTest
{
	public static void main(String[] args) 
	{
		SeriesExecutor executor = new SeriesExecutor();
		executor.execute(() -> System.out.println("asdfasdfasdf"));
		executor.execute(() -> System.out.println("asdfasdfasdf2"));
		executor.execute(() -> System.out.println("asdfasdfasdf3"));
		executor.shutDown();
	}
}
