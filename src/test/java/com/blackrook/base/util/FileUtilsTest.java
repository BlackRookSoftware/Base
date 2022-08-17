package com.blackrook.base.util;

import java.io.File;

public final class FileUtilsTest 
{
	public static void main(String[] args)
	{
		File file0 = new File("abc.xyz");
		File file1 = new File("abc");
		File file2 = new File("someplace/abc.xyz");
		File file3 = new File("someplace/abc");
		System.out.println(FileUtils.addExtension(file0, "png"));
		System.out.println(FileUtils.addMissingExtension(file0, "png"));
		System.out.println(FileUtils.changeExtension(file0, "png"));

		System.out.println(FileUtils.addExtension(file1, "png"));
		System.out.println(FileUtils.addMissingExtension(file1, "png"));
		System.out.println(FileUtils.changeExtension(file1, "png"));

		System.out.println(FileUtils.addExtension(file2, "png"));
		System.out.println(FileUtils.addMissingExtension(file2, "png"));
		System.out.println(FileUtils.changeExtension(file2, "png"));

		System.out.println(FileUtils.addExtension(file3, "png"));
		System.out.println(FileUtils.addMissingExtension(file3, "png"));
		System.out.println(FileUtils.changeExtension(file3, "png"));
	}
}
