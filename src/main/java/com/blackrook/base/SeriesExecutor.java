package com.blackrook.base;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An executor type that executes tasks single-threadedly in the order that they were added.
 * @author Matthew Tropiano
 */
public class SeriesExecutor implements Executor
{
	private static final String DEFAULT_NAME = "SeriesExecutorWorker";
	private static final AtomicLong WORKER_ID = new AtomicLong(0L);

	/** The job queue. */
	private BlockingQueue<Runnable> queue;
	/** The worker thread. */
	private Worker worker;
	
	/**
	 * Creates a new series executor with a default name, as a daemon executor.
	 * @see Thread#setDaemon(boolean)
	 */
	public SeriesExecutor()
	{
		this(DEFAULT_NAME + "-" + WORKER_ID.getAndIncrement(), true);
	}
	
	/**
	 * Creates a new series executor, as a daemon executor.
	 * @param name the name of the worker thread.
	 */
	public SeriesExecutor(String name)
	{
		this(name, true);
	}
	
	/**
	 * Creates a new series executor with a default name.
	 * @param daemon if the worker thread is a daemon thread.
	 */
	public SeriesExecutor(boolean daemon)
	{
		this(DEFAULT_NAME + "-" + WORKER_ID.getAndIncrement(), daemon);
	}
	
	/**
	 * Creates a new series executor.
	 * @param name the name of the worker thread.
	 * @param daemon if the worker thread is a daemon thread.
	 * @see Thread#setDaemon(boolean)
	 */
	public SeriesExecutor(String name, boolean daemon)
	{
		this.queue = new LinkedBlockingQueue<>();
		(worker = new Worker(name, daemon)).start();
	}
	
	@Override
	public void execute(Runnable command) 
	{
		queue.offer(command);
	}
	
	/**
	 * Shuts down the worker on this executor.
	 */
	public void shutDown()
	{
		worker.shutDown();
	}

	// Worker thread.
	private class Worker extends Thread
	{
		private volatile boolean keepGoing;
		
		private Worker(String name, boolean daemon)
		{
			super(name);
			setDaemon(daemon);
			keepGoing = true;
		}
		
		private void shutDown()
		{
			keepGoing = false;
			interrupt();
		}
		
		@Override
		public void run() 
		{
			while (keepGoing)
			{
				Runnable task;
				try {
					task = queue.take();
				} catch (InterruptedException e) {
					break;
				}
				
				try {
					task.run();
				} catch (Throwable t) {
					// Eat exception.
				}
			}
		}
	}
	
}
