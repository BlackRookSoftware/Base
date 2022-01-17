package com.blackrook.base;

import java.io.File;

public final class ProcessCallableTest 
{
	public static void main(String[] args) throws Exception
	{
		ProcessCallable.cmd("dir", "/b", "/o", "/s").setWorkingDirectory(new File(".")).inheritOut().call();
	}

}
