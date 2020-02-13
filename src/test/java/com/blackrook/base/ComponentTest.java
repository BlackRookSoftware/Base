/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
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
		FordCar car = manager.get(FordCar.class);
		Iterable<Drivable> drivables = manager.getWithType(Drivable.class);
		Iterable<Stealable> stealables = manager.getWithType(Stealable.class);
		manager.getWithType(Loggable.class).forEach((l)->l.log("Butts LOL"));
	}
}
