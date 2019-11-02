package com.blackrook.base;

import com.blackrook.base.componenttest.Drivable;
import com.blackrook.base.componenttest.FordCar;
import com.blackrook.base.componenttest.Loggable;
import com.blackrook.base.componenttest.Stealable;

public final class ComponentTest
{
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		ComponentManager manager = ComponentManager.create("com.blackrook.base.componenttest");
		FordCar car = manager.getSingleton(FordCar.class);
		Iterable<Drivable> drivables = manager.getSingletonsWithType(Drivable.class);
		Iterable<Stealable> stealables = manager.getSingletonsWithType(Stealable.class);
		for (Loggable l : manager.getSingletonsWithType(Loggable.class))
			l.log("Butts LOL");
		System.out.println();
	}
}
