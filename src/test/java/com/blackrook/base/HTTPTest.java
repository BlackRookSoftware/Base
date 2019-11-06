package com.blackrook.base;

import java.io.IOException;

import com.blackrook.base.util.HTTPUtils;

public final class HTTPTest
{
	public static void main(String[] args) throws IOException
	{
		for (int i = 0; i < 10; i++)
			System.out.println(HTTPUtils.httpGet("https://www.doomworld.com/idgames/api/api.php?action=comic", 10000, HTTPUtils.STRING_CONTENT_READER));
	}

}
