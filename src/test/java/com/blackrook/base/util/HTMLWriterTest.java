package com.blackrook.base.util;

import java.io.File;

import com.blackrook.base.HTMLWriter;
import com.blackrook.base.HTMLWriter.Options;

import static com.blackrook.base.HTMLWriter.attribute;

public final class HTMLWriterTest 
{
	public static void main(String[] args) throws Exception
	{
		System.out.println(HTMLWriter.createHTMLString("html", Options.PRETTY, Options.SLASHES_IN_SINGLE_TAGS)
			.push("html")
				.push("head")
					.tag("title", "UNNAMED \"Test\" Page")
					.tag("link", attribute("rel", "StyleSheet"), attribute("href", "css/stuff.css"))
				.pop()
				.push("body", attribute("class", "content-body"))
					.comment("This is a comment.")
					.tag("pre", new File("build.xml"))
				.pop()
			.pop()
		);
	}
}
