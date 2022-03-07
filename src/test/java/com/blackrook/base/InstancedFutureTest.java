/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import static com.blackrook.base.InstancedFuture.*;

public final class InstancedFutureTest 
{
	public static void main(String[] args) throws Exception
	{
		instance(() -> {
			return 4 + 5;
		})
		.onResult((num) -> System.out.println(num))
		.onError((exception) -> exception.printStackTrace(System.err))
		.onComplete(() -> System.out.println("Done!"))
		.spawn();
	}

}
