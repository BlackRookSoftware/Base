/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public final class EncodeTest 
{
	public static void main(String[] args) throws Exception
	{
		String str = "háček";
		byte[] strbytes = str.getBytes();
		String base64 = EncodingUtils.asBase64(new ByteArrayInputStream(strbytes));
		byte[] decode = EncodingUtils.fromBase64(base64);
		System.out.println(str);
		System.out.println(base64);
		System.out.println(new String(EncodingUtils.fromBase64(base64)));
		System.out.println(Arrays.toString(strbytes));
		System.out.println(Arrays.toString(decode));
		
		strbytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
		base64 = EncodingUtils.asBase64(new ByteArrayInputStream(strbytes));
		decode = EncodingUtils.fromBase64(base64);
		System.out.println(Arrays.toString(strbytes));
		System.out.println(base64);
		System.out.println(Arrays.toString(decode));
	}
}
