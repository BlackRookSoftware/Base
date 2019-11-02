package com.blackrook.base.componenttest;

import com.blackrook.base.ComponentManager.ComponentConstructor;
import com.blackrook.base.ComponentManager.ComponentFactory;
import com.blackrook.base.ComponentManager.ComponentProvider;
import com.blackrook.base.ComponentManager.ConstructingClass;
import com.blackrook.base.ComponentManager.NonSingleton;
import com.blackrook.base.LoggingFactory;

@ComponentFactory
public class LogFactory extends LoggingFactory
{
	@ComponentConstructor
	public LogFactory()
	{
		super(new ConsoleLogger());
	}
	
	@ComponentProvider
	@NonSingleton
	public Logger getLogger(@ConstructingClass Class<?> forClass)
	{
		return super.getLogger(forClass);
	}
	
}
