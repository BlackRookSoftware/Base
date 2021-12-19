/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import com.blackrook.base.http.HTTP1.Version;
import com.blackrook.base.util.IOUtils;

public final class HTTP1Test
{
	public static void main(String[] args)
	{
		Socket socket = null;
		HTTP1.Writer clientWriter = null;
		HTTP1.Reader clientReader = null;
		
		try {
			socket = new Socket("mtrop.net", 80);
			
			clientWriter = new HTTP1.Writer(socket.getOutputStream());
			clientReader = new HTTP1.Reader(socket.getInputStream());

			// Send request.
			clientWriter.writeRequestHeader("GET", "/", Version.HTTP11);
			clientWriter.writeHeader("Host", "mtrop.net");
			clientWriter.writeHeader("Accept", "text/html");
			clientWriter.writeHeader("User-Agent", "BlackRookHTTP1/1.0 (Java 11)");
			clientWriter.writeHeader("Content-Length", "0");
			clientWriter.writeCRLF();
			clientWriter.flush();
			
			// Read response header.
			System.out.println(clientReader.readResponseHeader());
			HTTP1.Reader.Header header;
			while ((header = clientReader.readHeader()) != null)
				System.out.println(header);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(clientReader.getInputStream(), "ISO-8859-1"));
			String line;
			while ((line = br.readLine()) != null)
				System.out.println(line);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			IOUtils.close(clientReader);
			IOUtils.close(clientWriter);
			IOUtils.close(socket);
		}
	}
}
