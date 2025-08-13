/*******************************************************************************
 * Copyright (c) 2019-2025 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.blackrook.base.util.HTTPUtils.HTTPCookie.SameSiteMode;
import com.blackrook.base.util.HTTPUtils.HTTPReader;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;

import static com.blackrook.base.util.HTTPUtils.*;
import static com.blackrook.base.util.HTTPUtils.HTTPContent.*;

/**
 * Uses a local instance of httpbin.org to test.
 * @author Matthew Tropiano
 */
@SuppressWarnings("unused")
public final class HTTPUtilsTest2
{
	private static final String TEST_URL_BASE = "http://mtrop.net";
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException 
	{
		HTTPResponse response = HTTPRequest.get(TEST_URL_BASE)
			.setHeader("Accept", "text/html")
			.setHeader("Accept-Encoding", "gzip")
			.send();
		System.out.println(response.getRedirectHistory());
		System.out.println(response.getStatusCode() + ": " + response.getStatusMessage());
		System.out.println(response.decode().read(HTTPReader.createLineConsumer(System.out::println)));
	}

}
