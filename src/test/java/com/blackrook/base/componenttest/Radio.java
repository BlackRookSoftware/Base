/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.componenttest;

import com.blackrook.base.ComponentManager.Component;
import com.blackrook.base.ComponentManager.ComponentConstructor;
import com.blackrook.base.ComponentManager.Singleton;
import com.blackrook.base.LoggingFactory.Logger;

@Component
@Singleton
public class Radio implements Loggable, Stealable
{
	private Logger logger;
	
	@ComponentConstructor
	public Radio(Logger logger)
	{
		this.logger = logger;
	}
	
	@Override
	public void log(String bluh)
	{
		logger.info(bluh);
	}
	
}
