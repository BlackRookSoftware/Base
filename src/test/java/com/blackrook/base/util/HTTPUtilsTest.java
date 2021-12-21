package com.blackrook.base.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.blackrook.base.util.HTTPUtils.HTTPCookie.SameSiteMode;
import com.blackrook.base.util.HTTPUtils.HTTPReader;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;

import static com.blackrook.base.util.HTTPUtils.*;

@SuppressWarnings("unused")
public final class HTTPUtilsTest
{
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException 
	{
		HTTPResponse response = absoluteredirect().send();
		System.out.println(response.getRedirectHistory());
		response.read(HTTPReader.createLineConsumer(System.out::println));
	}
	
	private static HTTPRequest httpget()
	{
		return HTTPRequest.get("http://localhost:8080/get?a=5&b=7");
	}

	private static HTTPRequest httpput()
	{
		return HTTPRequest.put("http://localhost:8080/put")
			.content(
				HTTPUtils.createTextContent("application/json", "{\"x\":3, \"y\":\"farts\"}")
			)
		;
	}

	private static HTTPRequest httppost()
	{
		return HTTPRequest.post("http://localhost:8080/post")
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
	
	private static HTTPRequest httppatch()
	{
		return HTTPRequest.post("http://localhost:8080/patch?a=5&b=7")
			.headers(
				entry("X-HTTP-Method-Override", "PATCH")
			)
			.content(
				createTextContent("application/json", "{\"x\":3, \"y\":\"farts\"}")
			)
		;
	}

	private static HTTPRequest httpdelete()
	{
		return HTTPRequest.delete("http://localhost:8080/delete?a=5&b=7");
	}

	private static HTTPRequest redirect()
	{
		return HTTPRequest.get("http://localhost:8080/redirect/7");
	}

	private static HTTPRequest absoluteredirect()
	{
		return HTTPRequest.get("http://localhost:8080/absolute-redirect/7");
	}

	private static HTTPRequest relativeredirect()
	{
		return HTTPRequest.get("http://localhost:8080/relative-redirect/7");
	}

}
