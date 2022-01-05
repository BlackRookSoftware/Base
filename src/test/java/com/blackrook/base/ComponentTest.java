/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import com.blackrook.base.ComponentManager.Ordering;
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
		FordCar car = manager.get(FordCar.class);
		
		System.out.println("Drivables:");
		manager.getWithType(Drivable.class).forEach((x)->System.out.println(x.toString()));
		
		System.out.println("Stealables:");
		manager.getWithType(Stealable.class).forEach((x)->System.out.println(x.toString()));
		
		System.out.println("Ordered:");
		manager.getWithAnnotation(Ordering.class).forEach((x)->System.out.println(x.toString()));

		System.out.println("Loggables:");
		manager.getWithType(Loggable.class).forEach((l)->l.log("Butts LOL"));
	}
}
