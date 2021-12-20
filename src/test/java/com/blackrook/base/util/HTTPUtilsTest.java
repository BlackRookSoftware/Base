package com.blackrook.base.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.blackrook.base.AsyncFactory;
import com.blackrook.base.util.HTTPUtils.HTTPCookie.SameSiteMode;
import com.blackrook.base.util.HTTPUtils.HTTPRequest;

public final class HTTPUtilsTest
{
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException 
	{
		AsyncFactory async = new AsyncFactory();
		
		final ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("localhost", 8080));
		Future<Void> connectionHandler = async.spawn(() -> 
		{
			try {
				Socket socket = serverSocket.accept();
				socket.setSoTimeout(1000);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
				
				String line;
				while ((line = reader.readLine()) != null)
					System.out.println(line);
				
			} catch (SocketTimeoutException e) {
				// Die gracefully.
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});

		try {
			HTTPRequest.post("http://localhost:8080/").timeout(1000)
				.addHeaders(HTTPUtils.headers(
					HTTPUtils.entry("X-BUTTS", "è,é,ê,ë"),
					HTTPUtils.entry("Accept", "application/json")
				))
				.addParameters(HTTPUtils.parameters(HTTPUtils.entry("butt", "junk junk")))
				.addCookie(HTTPUtils.cookie("a", "2345").expires(System.currentTimeMillis() + (1000 * 60)).sameSite(SameSiteMode.STRICT))
				.content(
					HTTPUtils.createMultipartContent()
						.addTextFilePart("test1", "text/plain", new File("src/test/java/com/blackrook/base/util/HTTPUtilsTest.java"))
						.addField("test2", "asdfl;aksdfklasjd;lfkja;lksjdfja;sldkf")
						.addTextFilePart("test3", "text/plain", new File("src/test/java/com/blackrook/base/util/HTMLWriterTest.java"))
				)
			.send();
		} catch (SocketTimeoutException e) {
			// Die gracefully.
		}
		
		connectionHandler.get(); // join
		serverSocket.close();
	}
}
