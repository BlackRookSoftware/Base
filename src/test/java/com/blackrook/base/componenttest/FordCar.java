package com.blackrook.base.componenttest;

import com.blackrook.base.ComponentManager;
import com.blackrook.base.LoggingFactory.Logger;

@ComponentManager.Component
@ComponentManager.Singleton
@ComponentManager.Ordering(-1)
public class FordCar extends Vehicle implements Loggable, Stealable
{
	private Logger logger;
	private Radio radio;
	
	@ComponentManager.ComponentConstructor
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
