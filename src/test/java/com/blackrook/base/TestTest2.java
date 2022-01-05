/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import com.blackrook.base.util.TestUtils;
import com.blackrook.base.util.TestUtils.AfterEachTest;
import com.blackrook.base.util.TestUtils.BeforeAllTests;
import com.blackrook.base.util.TestUtils.BeforeEachTest;
import com.blackrook.base.util.TestUtils.Test;

public class TestTest2
{
	@BeforeAllTests
	public static void doBeforeAll()
	{
	}
	
	@BeforeEachTest
	public void doBeforeEach()
	{
	}
	
	@AfterEachTest
	public void doAfterEach()
	{
	}

	@Test
	public void doTest()
	{
		TestUtils.assertEqual(1, 1);
		TestUtils.assertEqual(5, ()->5);
		TestUtils.assertException(()->{
			throw new RuntimeException();
		});
		TestUtils.assertExceptionType(Exception.class, ()->{
			throw new RuntimeException();
		});
	}
}
