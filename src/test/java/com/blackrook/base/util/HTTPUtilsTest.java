package com.blackrook.base.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.blackrook.base.util.HTTPUtils.HTTPReader;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;

public final class HTTPUtilsTest
{
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException 
	{
		HTTPRequest.get("http://mtrop.net").send(HTTPReader.createFileDownloader(new File("junk.html")));
		System.out.println("Wrote file.");
	}
}
