package com.blackrook.base.util;

import java.util.concurrent.ExecutionException;

import com.blackrook.base.util.HTTPUtils.HTTPReader;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;
import com.blackrook.base.util.HTTPUtils.HTTPRequestFuture;

public final class HTTPUtilsTest
{
	public static void main(String[] args) throws InterruptedException, ExecutionException 
	{
		HTTPRequestFuture<String> content2 = HTTPRequest.get("https://blackrooksoftware.github.io/").sendAsync(HTTPReader.STRING_CONTENT_READER);
		HTTPRequestFuture<String> content = HTTPRequest.get("https://google.com").sendAsync(HTTPReader.STRING_CONTENT_READER);
		System.out.println("Fetching....");
		System.out.println(content.getResponse().getStatusCode() + ": " + content.result());
		System.out.println(content2.getResponse().getStatusCode() + ": " + content2.result());
	}
}
