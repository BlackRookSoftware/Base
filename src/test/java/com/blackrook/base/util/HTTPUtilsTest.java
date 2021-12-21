package com.blackrook.base.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.blackrook.base.util.HTTPUtils.HTTPCookie.SameSiteMode;
import com.blackrook.base.util.HTTPUtils.HTTPReader;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;

public final class HTTPUtilsTest
{
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException 
	{
		System.out.println(HTTPRequest.post("http://localhost:8080/post").timeout(1000)
			.addHeaders(HTTPUtils.headers(
				HTTPUtils.entry("X-BUTTS", "è,é,ê,ë"),
				HTTPUtils.entry("Accept", "application/json")
			))
			.addParameters(HTTPUtils.parameters(
				HTTPUtils.entry("butt", "junk junk"),
				HTTPUtils.entry("crap", "è,é,ê,ë")
			))
			.addCookie(HTTPUtils.cookie("a", "2345").expires(System.currentTimeMillis() + (1000 * 60)).sameSite(SameSiteMode.STRICT))
			.content(
				HTTPUtils.createMultipartContent()
					.addTextFilePart("test1", "text/plain", new File("src/test/java/com/blackrook/base/util/HTTPUtilsTest.java"))
					.addField("test2", "asdfl;aksdfklasjd;lfkja;lksjdfja;sldkf")
					.addTextFilePart("test3", "text/plain", new File("src/test/java/com/blackrook/base/util/HTMLWriterTest.java"))
			)
		.send(HTTPReader.STRING_CONTENT_READER));
	}
	
}
