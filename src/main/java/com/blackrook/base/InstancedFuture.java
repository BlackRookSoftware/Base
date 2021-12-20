/*******************************************************************************
 * Copyright (c) 2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A single instance of a spawned, potentially asynchronous executable task.
 * Note that this class is a type of {@link RunnableFuture} - this can be used in places
 * that {@link Future}s can also be used.
 * @param <T> the result type.
 */
public abstract class InstancedFuture<T> implements RunnableFuture<T>
{
	// Locks
	private Object waitMutex;

	// State
	private Callable<T> callable;
	private Thread executor;
	private boolean done;
	private boolean running;
	protected boolean cancelled;
	private Throwable exception;
	private T finishedResult;

	/**
	 * Creates a new InstancedFuture with no callable payload.
	 */
	public InstancedFuture()
	{
		this(null);
	}
	
	/**
	 * Creates a new InstancedFuture.
	 * @param callable the callable to call.
	 */
	public InstancedFuture(Callable<T> callable)
	{
		this.cancelled = false;
		this.waitMutex = new Object();

		this.callable = callable;
		this.executor = null;
		this.done = false;
		this.running = false;
		this.exception = null;
		this.finishedResult = null;
	}
	
	@Override
	public final void run()
	{
		executor = Thread.currentThread();
		done = false;
		running = true;
		exception = null;
		finishedResult = null;

		try {
			finishedResult = execute();
		} catch (Throwable e) {
			exception = e;
		}
		
		executor = null;
		running = false;
		done = true;
		synchronized (waitMutex)
		{
			waitMutex.notifyAll();
		}
	}

	/**
	 * Checks if this task instance is being worked on by a thread.
	 * @return true if so, false if not.
	 */
	public final boolean isRunning()
	{
		return running;
	}

	@Override
	public final boolean isDone()
	{
		return done;
	}

	/**
	 * Gets the thread that is currently executing this future.
	 * If this is done or waiting for execution, this returns null.
	 * @return the executor thread, or null.
	 */
	public final Thread getExecutor()
	{
		return executor;
	}
	
	/**
	 * Makes the calling thread wait indefinitely for this task instance's completion.
	 * @throws InterruptedException if the current thread was interrupted while waiting.
	 */
	public final void waitForDone() throws InterruptedException
	{
		while (!isDone())
		{
			liveLockCheck();
			synchronized (waitMutex)
			{
				waitMutex.wait();
			}
		}
	}

	/**
	 * Makes the calling thread wait for this task instance's completion for, at most, the given interval of time.
	 * @param time the time to wait.
	 * @param unit the time unit of the timeout argument.
	 * @throws InterruptedException if the current thread was interrupted while waiting.
	 */
	public final void waitForDone(long time, TimeUnit unit) throws InterruptedException
	{
		if (!isDone())
		{
			liveLockCheck();
			synchronized (waitMutex)
			{
				unit.timedWait(waitMutex, time);
			}
		}
	}

	/**
	 * Gets the exception thrown as a result of this instance completing, making the calling thread wait for its completion.
	 * @return the exception thrown by the encapsulated task, or null if no exception.
	 */
	public final Throwable getException()
	{
		join();
		return exception;
	}

	@Override
	public final T get() throws InterruptedException, ExecutionException
	{
		liveLockCheck();
		waitForDone();
		if (isCancelled())
			throw new CancellationException("task was cancelled");
		if (getException() != null)
			throw new ExecutionException(getException());
		return finishedResult;
	}

	@Override
	public final T get(long time, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException
	{
		liveLockCheck();
		waitForDone(time, unit);
		if (!isDone())
			throw new TimeoutException("wait timed out");
		if (isCancelled())
			throw new CancellationException("task was cancelled");
		if (getException() != null)
			throw new ExecutionException(getException());
		return finishedResult;
	}

	/**
	 * Performs a {@link #get()} and on success, calls the success function.
	 * If an exception would have happened, the success function is not called.
	 * @param <R> the return type after the function call.
	 * @param onSuccess the function to call on success with the result object, returning the return object. 
	 * 		If null when this would be called, this returns null.
	 * @return the result from the success function, or null if an exception happened.
	 */
	public final <R> R getAndThen(Function<T, R> onSuccess)
	{
		return getAndThen(onSuccess, null);
	}
	
	/**
	 * Performs a {@link #get()} and on success, calls the success function, or calls the exception function on an exception.
	 * @param <R> the return type after the function.
	 * @param onSuccess the function to call on success with the result object, returning the return object. 
	 * 		If null when this would be called, this returns null.
	 * @param onException the function to call on exception. If null when this would be called, this returns null.
	 * @return the result from the success function, or the result from the exception function if an exception happened.
	 */
	public final <R> R getAndThen(Function<T, R> onSuccess, Function<Throwable, R> onException)
	{
		try {
			return onSuccess.apply(get());
		} catch (Exception e) {
			return onException != null ? onException.apply(exception) : null;
		}
	}
	
	/**
	 * Performs a {@link #get()} and on success, calls the success function.
	 * If an exception would have happened, the success function is not called.
	 * @param <R> the return type after the function call.
	 * @param time the maximum time to wait.
	 * @param unit the time unit of the timeout argument.
	 * @param onSuccess the function to call on success with the result object, returning the return object. 
	 * 		If null when this would be called, this returns null.
	 * @return the result from the success function, or null if an exception happened or a timeout occurred.
	 */
	public final <R> R getAndThen(long time, TimeUnit unit, Function<T, R> onSuccess)
	{
		return getAndThen(time, unit, onSuccess, null, null);
	}

	/**
	 * Performs a {@link #get()} and on success, calls the success function.
	 * If an exception would have happened, the success function is not called.
	 * @param <R> the return type after the function call.
	 * @param time the maximum time to wait.
	 * @param unit the time unit of the timeout argument.
	 * @param onSuccess the function to call on success with the result object, returning the return object. 
	 * 		If null when this would be called, this returns null.
	 * @param onTimeout the function to call on wait timeout. First Parameter is the timeout in milliseconds. If null when this would be called, this returns null.
	 * @return the result from the success function, or null if an exception happened.
	 */
	public final <R> R getAndThen(long time, TimeUnit unit, Function<T, R> onSuccess, Function<Long, R> onTimeout)
	{
		return getAndThen(time, unit, onSuccess, onTimeout, null);
	}

	/**
	 * Performs a {@link #get()} and on success, calls the success function, or calls the exception function on an exception.
	 * @param <R> the return type after the function.
     * @param time the maximum time to wait.
     * @param unit the time unit of the timeout argument.
	 * @param onSuccess the function to call on success with the result object, returning the return object.
	 * @param onTimeout the function to call on wait timeout. First Parameter is the timeout in milliseconds. If null when this would be called, this returns null.
	 * @param onException the function to call on exception. If null when this would be called, this returns null.
	 * @return the result from the success function, or the result from the exception function if an exception happened.
	 */
	public final <R> R getAndThen(long time, TimeUnit unit, Function<T, R> onSuccess, Function<Long, R> onTimeout, Function<Throwable, R> onException)
	{
		try {
			return onSuccess != null ? onSuccess.apply(get(time, unit)) : null;
		} catch (TimeoutException e) {
			return onTimeout != null ? onTimeout.apply(TimeUnit.MILLISECONDS.convert(time, unit)) : null;
		} catch (Exception e) {
			return onException != null ? onException.apply(exception) : null;
		}
	}
	
	/**
	 * Attempts to return the result of this instance, making the calling thread wait for its completion.
	 * <p>This is for convenience - this is like calling {@link #get()}, except it will only throw an
	 * encapsulated {@link RuntimeException} with an exception that {@link #get()} would throw as a cause.
	 * @return the result. Can be null if no result is returned, or this was cancelled before the return.
	 * @throws RuntimeException if a call to {@link #get()} instead of this would throw an exception.
	 * @throws IllegalStateException if the thread processing this future calls this method.
	 */
	public final T result()
	{
		liveLockCheck();
		try {
			waitForDone();
		} catch (InterruptedException e) {
			throw new RuntimeException("wait was interrupted", getException());
		}
		if (getException() != null)
			throw new RuntimeException("exception on result", getException());
		return finishedResult;
	}

	/**
	 * Attempts to return the result of this instance, without waiting for its completion.
	 * <p>If {@link #isDone()} is false, this is guaranteed to return <code>null</code>.
	 * @return the current result, or <code>null</code> if not finished.
	 */
	public final T resultNonBlocking()
	{
		return finishedResult;
	}

	/**
	 * Makes the calling thread wait until this task has finished, returning nothing.
	 * This differs from {@link #waitForDone()} such that it eats a potential {@link InterruptedException}.
	 * @throws IllegalStateException if the thread processing this future calls this method.
	 */
	public final void join()
	{
		try {
			waitForDone();
		} catch (Exception e) {
			// Eat exception.
		}
	}

    /**
     * Convenience method for: <code>cancel(false)</code>.
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
	public final boolean cancel()
	{
		return cancel(false);
	}

	@Override
	public final boolean cancel(boolean mayInterruptIfRunning)
	{
		if (isDone())
		{
			return false;
		}
		else
		{
			if (mayInterruptIfRunning && executor != null)
				executor.interrupt();
			cancelled = true;
			join();
			return true;
		}
	}

	@Override
	public final boolean isCancelled() 
	{
		return cancelled;
	}
	
	/**
	 * Executes this instance's callable payload.
	 * @return the result from the execution.
	 * @throws Throwable for any exception that may occur.
	 */
	protected T execute() throws Throwable
	{
		return callable != null ? callable.call() : null;
	}

	// Checks for livelocks.
	private void liveLockCheck()
	{
		if (executor == Thread.currentThread())
			throw new IllegalStateException("Attempt to make executing thread wait for this result.");
	}

}

