package com.blackrook.base.componenttest;

import com.blackrook.base.ComponentManager.Component;
import com.blackrook.base.ComponentManager.ComponentConstructor;
import com.blackrook.base.ComponentManager.Singleton;
import com.blackrook.base.LoggingFactory.Logger;

@Component
@Singleton
public class FordCar extends Vehicle implements Loggable, Stealable
{
	private Logger logger;
	private Radio radio;
	
	@ComponentConstructor
	public FordCar(Logger logger, Radio radio)
	{
		this.radio = radio;
		this.logger = logger;
	}
	
	public Radio getRadio()
	{
		return radio;
	}
	
	@Override
	public void log(String bluh)
	{
		logger.info(bluh);
	}
	
}
