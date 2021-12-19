package com.blackrook.base.util;

import java.util.concurrent.ExecutionException;

import com.blackrook.base.util.HTTPUtils.HTTPReader;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;
import com.blackrook.base.util.HTTPUtils.HTTPRequestFuture;

public final class HTTPUtilsTest
{
	public static void main(String[] args) throws InterruptedException, ExecutionException 
	{
		HTTPRequestFuture<String> content = HTTPRequest.get("http://mtrop.net").sendAsync(HTTPReader.STRING_CONTENT_READER);
		System.out.println("Fetching....");
		if (content.getException() != null)
			content.getException().printStackTrace(System.err);
		else
		{
			System.out.println(content.getResponse().getStatusCode() + ": " + content.result());
			System.out.println(content.getResponse().getRedirectHistory());
		}
	}
}
