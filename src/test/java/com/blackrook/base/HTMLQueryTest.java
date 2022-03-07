/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.Reader;

import com.blackrook.base.util.HTTPUtils.HTTPRequest;

@SuppressWarnings("unused")
public final class HTMLQueryTest
{
	public static void main(String[] args) throws Exception 
	{
		try (Reader reader = HTTPRequest.get("https://doomworld.com").send().getContentReader())
		{
			HTMLQuery query = HTMLQuery.readDocument(reader);
			System.out.print("");
		}
	}
}
