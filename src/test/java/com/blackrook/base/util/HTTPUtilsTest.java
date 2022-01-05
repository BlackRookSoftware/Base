/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
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
public final class HTTPUtilsTest
{
	private static final String TEST_URL_BASE = "http://localhost:8080";
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException 
	{
		HTTPResponse response = links().send();
		System.out.println(response.getRedirectHistory());
		System.out.println(response.getStatusCode() + ": " + response.getStatusMessage());
		response.read(HTTPReader.createLineConsumer(System.out::println));
		//response.read(HTTPReader.createByteConsumer(System.out::print));
	}
	
	/* ==================================================================== */

	private static String url(String path)
	{
		return TEST_URL_BASE + path;
	}
	
	/* ==================================================================== */

	private static HTTPRequest httpGet()
	{
		return HTTPRequest.get(url("/get?a=5&b=7"));
	}

	private static HTTPRequest httpPut()
	{
		return HTTPRequest.put(url("/put"))
			.content(createTextContent("application/json", "{\"x\":3, \"y\":\"farts\"}"))
		;
	}

	private static HTTPRequest httpPost()
	{
		return HTTPRequest.post(url("/post"))
			.headers(
				entry("X-BUTTS", "è,é,ê,ë"),
				entry("Accept", "application/json")
			)
			.parameters(
				entry("butt", "junk junk"),
				entry("crap", "è,é,ê,ë")
			)
			.addCookie(cookie("a", "2345")
				.expires(System.currentTimeMillis() + (1000 * 60))
				.sameSite(SameSiteMode.STRICT)
			)
			.content(createMultipartContent()
				.addTextFilePart("test1", "text/plain", new File("src/test/java/com/blackrook/base/util/HTTPUtilsTest.java"))
				.addField("test2", "asdfl;aksdfklasjd;lfkja;lksjdfja;sldkf")
				.addTextFilePart("test3", "text/plain", new File("src/test/java/com/blackrook/base/util/HTMLWriterTest.java"))
			)
		;
	}
	
	private static HTTPRequest httpPatch()
	{
		return HTTPRequest.post(url("/patch?a=5&b=7"))
			.headers(
				entry("X-HTTP-Method-Override", "PATCH")
			)
			.content(
				createTextContent("application/json", "{\"x\":3, \"y\":\"farts\"}")
			)
		;
	}

	private static HTTPRequest httpDelete()
	{
		return HTTPRequest.delete(url("/delete"))
			.parameters(
				entry("a", 5), 
				entry("b", 7)
			)
		;
	}

	/* ==================================================================== */

	private static HTTPRequest auth()
	{
		return HTTPRequest.get(url("/basic-auth/butt/butts"));
	}
	
	/* ==================================================================== */

	private static HTTPRequest delayGet()
	{
		return HTTPRequest.get(url("/delay/2"));
	}
	
	private static HTTPRequest delayPost()
	{
		return HTTPRequest.post(url("/delay/2"));
	}

	private static HTTPRequest delayPut()
	{
		return HTTPRequest.put(url("/delay/2"));
	}

	private static HTTPRequest delayDelete()
	{
		return HTTPRequest.delete(url("/delay/2"));
	}

	private static HTTPRequest drip()
	{
		return HTTPRequest.get(url("/drip"))
			.parameters(entry("numbytes", 100))
			.timeout(0)
		;
	}

	private static HTTPRequest links()
	{
		return HTTPRequest.get(url("/links/10/0"));
	}

	/* ==================================================================== */
	
	private static HTTPRequest redirect()
	{
		return HTTPRequest.get(url("/redirect/7"));
	}

	private static HTTPRequest absoluteredirect()
	{
		return HTTPRequest.get(url("/absolute-redirect/7"));
	}

	private static HTTPRequest relativeredirect()
	{
		return HTTPRequest.get(url("/relative-redirect/7"));
	}

}
