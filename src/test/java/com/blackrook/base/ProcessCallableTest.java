/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.File;

public final class ProcessCallableTest 
{
	public static void main(String[] args) throws Exception
	{
		ProcessCallable.cmd("dir", "/b", "/o", "/s").setWorkingDirectory(new File(".")).inheritOut().call();
	}

}
