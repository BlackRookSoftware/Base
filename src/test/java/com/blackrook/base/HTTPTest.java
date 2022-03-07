/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.blackrook.base.util.HTTPUtils.HTTPContent;

public final class HTTPTest
{
		
	public static void main(String[] args) throws IOException
	{
		HTTPContent content = HTTPContent.createTextFileContent("text/plain", new File("LICENSE.txt"));
		try (FileOutputStream fos = new FileOutputStream(new File("junk.txt")); InputStream in = content.getInputStream())
		{
			relay(in, fos, 8192, -1);
		}
		System.out.println(content.getLength());
	}

	private static int relay(InputStream in, OutputStream out, int bufferSize, int maxLength) throws IOException
	{
		int total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[bufferSize];
		
		while ((buf = in.read(RELAY_BUFFER, 0, Math.min(maxLength < 0 ? Integer.MAX_VALUE : maxLength, bufferSize))) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
			if (maxLength >= 0)
				maxLength -= buf;
		}
		return total;
	}

}
