/*******************************************************************************
 * Copyright (c) 2009-2026 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

/**
 * Ticker class that keeps a steady rate of ticks per second.
 * It spawns a thread on starting that does its best to maintain a constant
 * tick rate while keeping the time between ticks as accurate as possible (to the millisecond).
 * @author Matthew Tropiano
 */
public abstract class Ticker
{
	/** The name of this ticker. */
	private String name;
	/** The ticker's updates per second. */
	private int updatesPerSecond;
	
	/** The ticker thread. Spawned when started, killed when stopped. */
	private TickerThread ticker;
	/** The current tick count. */
	private long currentTick;
	/** Is this suspended? */
	private boolean suspend;
	/** Milliseconds to wait. */
	private long millis;
	/** Remainder nanoseconds to wait (fraction of milliseconds). */
	private long nanos;
	
	/** 
	 * Creates a new Ticker that updates at a constant rate.
	 * @param updatesPerSecond the target amount of times that doTick() is called in one second. 
	 */
	public Ticker(int updatesPerSecond)
	{
		this(null, updatesPerSecond);
	}
	
	/** 
	 * Creates a new Ticker that updates at a constant rate.
	 * @param name the name of the ticker. This is also the thread name.
	 * @param updatesPerSecond the target amount of times that doTick() is called in one second. 
	 */
	public Ticker(String name, int updatesPerSecond)
	{
		setName(name);
		setUpdatesPerSecond(updatesPerSecond);
		this.currentTick = 0;
	}
	
	/**
	 * Sets the name of the Ticker.
	 * @param name the name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
		if (ticker != null)
			ticker.setName(name);
	}
	
	/**
	 * Gets the name of this ticker.
	 * @return this ticker's name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the updates per second of the Ticker.
	 * @param updatesPerSecond the desired updates per second.
	 */
	public void setUpdatesPerSecond(int updatesPerSecond)
	{
		this.updatesPerSecond = updatesPerSecond;
		double millisPerUpdate = updatesPerSecond != 0 ? (1000/(double)updatesPerSecond) : 0;
		this.millis = (long)millisPerUpdate;
		this.nanos = (long)((millisPerUpdate-millis)*1000000);
	}
	
	/**
	 * Gets the updates per second of the Ticker.
	 * @return the desired updates per second set on this ticker.
	 */
	public int getUpdatesPerSecond()
	{
		return updatesPerSecond;
	}
	
	/**
	 * Called on each tick this is where work should be done each tick.
	 * @param tick	the current game tick useful for jobs to be performed on staggered intervals.
	 * 				the value of tick always increases by one each call.
	 */
	public abstract void doTick(long tick);
	
	/**
	 * Starts the ticker and a new instance of the internal Thread.
	 * Calling this while the ticker is active does nothing.
	 * Calling this while the ticker is suspended resumes it.
	 */
	public void start()
	{
		if (ticker != null)
			setSuspended(false);
		else
		{
			ticker = new TickerThread();
			if (name != null)
				ticker.setName(name);
			ticker.start();
		}
	}

	/**
	 * Stops the game ticker entirely, clearing its state and running out the internal Thread.
	 */
	public void stop()
	{
		if (ticker != null)
			ticker.killswitch = true;
		ticker = null;
	}

	/**
	 * Sets if this ticker is suspended or not.
	 * If true, the thread still operates but does not call doTick().
	 * If false, the thread operates normally.
	 * Tickers do NOT start suspended unless this was set before it was started.
	 * It is less expensive, and probably more desirable to supend and resume
	 * the Ticker than to start and stop it.
	 * @param value if true, suspend. if false, don't suspend.
	 */
	public void setSuspended(boolean value)
	{
		suspend = value;
	}
	
	/**
	 * Is this ticker suspended?
	 * @return true if so, false if not.
	 */
	public boolean isSuspended()
	{
		return suspend;
	}
	
	/**
	 * Is the ticker active?
	 * @return true if so, false if not.
	 */
	public boolean isActive()
	{
		return ticker != null && ticker.isAlive();
	}
	
	/**
	 * The ticker Thread.
	 */
	private class TickerThread extends Thread
	{
		boolean killswitch;
		
		public TickerThread()
		{
			super();
			setDaemon(true);
			suspend = false;
			killswitch = false;
		}
		
		@Override
		public void run()
		{
			long nanoCount = 0;
			long lastNanos = System.nanoTime();

			while (!killswitch)
			{
				long totalNanos = millis*1000000 + nanos;

				long nt = System.nanoTime();
				nanoCount += nt - lastNanos;
				lastNanos = nt;
				
				if (totalNanos == 0 || nanoCount >= totalNanos)
				{
					nanoCount -= totalNanos;
					
					if (!suspend)
						doTick(currentTick++);
				}
				
				try {
					Thread.sleep(0, 500000);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
	
}
