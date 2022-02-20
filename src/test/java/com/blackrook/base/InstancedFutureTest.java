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
