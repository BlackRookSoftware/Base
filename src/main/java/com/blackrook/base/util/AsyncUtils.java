/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for simple asynchronous tasks.
 * <p>This class uses an internal thread pool to facilitate asynchronous operations. All {@link Instance}s that the
 * "spawn" methods return are handles to the running routine, and are equivalent to {@link Future}s.
 * @author Matthew Tropiano
 */
public final class AsyncUtils
{
	/** Default amount of core threads. */
	public static final int DEFAULT_CORE_SIZE = 5;
	
	/** Default amount of max threads. */
	public static final int DEFAULT_MAX_SIZE = 10;

	/** Default keepalive time. */
	public static final long DEFAULT_KEEPALIVE_TIME = 5;

	/** Default keepalive time unit. */
	public static final TimeUnit DEFAULT_KEEPALIVE_TIMEUNIT = TimeUnit.SECONDS;
	
	// The static Thread Pool.
	private static final ThreadPoolExecutor threadPool;
	
	// No Process Error Listeners
	private static final ProcessStreamErrorListener[] NO_LISTENERS = new ProcessStreamErrorListener[0];

	static {
		threadPool = new ThreadPoolExecutor(
			DEFAULT_CORE_SIZE, 
			DEFAULT_MAX_SIZE, 
			DEFAULT_KEEPALIVE_TIME, 
			DEFAULT_KEEPALIVE_TIMEUNIT,
			new LinkedBlockingQueue<Runnable>(),
			new DefaultThreadFactory()
		);
	}
	
	/**
	 * Sets the amount of core threads in the thread pool.
	 * @param size the amount of threads.
	 */
	public static void setCoreThreads(int size)
	{
		threadPool.setCorePoolSize(size);
	}
	
	/**
	 * Sets the max amount of threads that the thread pool can expand to.
	 * @param size the amount of threads.
	 */
	public static void setMaxThreads(int size)
	{
		threadPool.setMaximumPoolSize(size);
	}

	/**
	 * Sets how long the threads created past the core amount will stay alive until they are ended. 
	 * @param time the time amount.
	 * @param unit the time unit.
	 */
	public static void setKeepAliveTime(long time, TimeUnit unit)
	{
		threadPool.setKeepAliveTime(time, unit);
	}

	/**
	 * A listener interface for {@link Monitorable} tasks. 
	 */
	@FunctionalInterface
	public static interface MonitorableListener
	{
		/**
		 * Called when the task reports a progress or message change in any way.
		 * @param indeterminate if true, progress is indeterminate - calculating it may result in a bad value.
		 * @param current the current progress.
		 * @param maximum the maximum progress.
		 * @param message the message.
		 */
		void onProgressChange(boolean indeterminate, double current, double maximum, String message);
	}

	/**
	 * A listener interface for {@link Process} tasks. 
	 */
	@FunctionalInterface
	public static interface ProcessStreamErrorListener
	{
		/** The stream type that produced the error. */
		enum StreamType
		{
			STDIN,
			STDOUT,
			STDERR
		}
		
		/**
		 * Called when a Process task reports an error.
		 */
		void onStreamError(StreamType type, Exception exception);
	}

	/**
	 * Spawns a Process, returning its return value.
	 * @param process the process to monitor - it should already be started.
	 * @return the new instance.
	 */
	public static Instance<Integer> spawn(Process process)
	{
		return spawn(process, null, null, null, NO_LISTENERS);
	}
	
	/**
	 * Spawns a Process with Standard Input attached, returning its return value.
	 * <p>This will spawn a Runnable for each provided stream, which will each be responsible for piping data into the process and
	 * reading from it. The runnables terminate when the streams close. The streams also do not attach if the I/O is redirected
	 * ({@link Process#getOutputStream()} returns <code>null</code>).
	 * <p>It is important to close the input stream, or else the process may hang, waiting forever for input.
	 * @param process the process to monitor - it should already be started.
	 * @param stdin the Standard IN stream. If null, no input is provided.
	 * @param stdout the Standard OUT/ERROR stream. If null, no output is provided.
	 * @return the new instance.
	 */
	public static Instance<Integer> spawn(Process process, InputStream stdin, OutputStream stdout, ProcessStreamErrorListener ... listeners)
	{
		return spawn(process, stdin, stdout, stdout, listeners);
	}
	
	/**
	 * Spawns a Process with the attached streams, returning its return value.
	 * <p>This will spawn a Runnable for each provided stream, which will each be responsible for piping data into the process and
	 * reading from it. The runnables terminate when the streams close. The streams also do not attach if the I/O is redirected
	 * ({@link Process#getInputStream()}, {@link Process#getErrorStream()}, or {@link Process#getOutputStream()} return <code>null</code>).
	 * <p>If the end of the provided input stream is reached or an error occurs, the pipe into the process is closed.
	 * @param process the process to monitor - it should already be started.
	 * @param stdin the Standard IN stream. If null, no input is provided.
	 * @param stdout the Standard OUT stream. If null, no output is provided.
	 * @param stderr the Standard ERROR stream. If null, no error output is provided.
	 * @return the new instance.
	 */
	public static Instance<Integer> spawn(
		Process process, 
		final InputStream stdin,
		final OutputStream stdout, 
		final OutputStream stderr, 
		final ProcessStreamErrorListener ... listeners
	)
	{
		final OutputStream stdInPipe = process.getOutputStream();
		final InputStream stdOutPipe = process.getInputStream();
		final InputStream stdErrPipe = process.getErrorStream();
		
		// Standard In
		if (stdin != null && stdInPipe != null) spawn(()->{
			try {
				relay(stdin, stdInPipe);
			} catch (IOException e) {
				for (int i = 0; i < listeners.length; i++)
					listeners[i].onStreamError(ProcessStreamErrorListener.StreamType.STDIN, e);
			} finally {
				close(stdInPipe);
			}
		});

		// Standard Out
		if (stdout != null && stdOutPipe != null) spawn(()->{
			try {
				relay(stdOutPipe, stdout);
			} catch (IOException e) {
				for (int i = 0; i < listeners.length; i++)
					listeners[i].onStreamError(ProcessStreamErrorListener.StreamType.STDOUT, e);
			}
		});
		
		// Standard Error
		if (stderr != null && stdErrPipe != null) spawn(()->{
			try {
				relay(stdErrPipe, stderr);
			} catch (IOException e) {
				for (int i = 0; i < listeners.length; i++)
					listeners[i].onStreamError(ProcessStreamErrorListener.StreamType.STDERR, e);
			}
		});
		
		Instance<Integer> out = new ProcessInstance(process);
		threadPool.execute(out);
		return out;
	}
	

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public static Instance<Void> spawn(Runnable runnable)
	{
		return spawn(runnable, (Void)null);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param runnable the runnable to use.
	 * @param result the result to set on completion.
	 * @return the new instance.
	 */
	public static <T> Instance<T> spawn(Runnable runnable, T result)
	{
		Instance<T> out = new RunnableInstance<T>(runnable, result);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Callable}.
	 * @param callable the callable to use.
	 * @return the new instance.
	 */
	public static <T> Instance<T> spawn(Callable<T> callable)
	{
		Instance<T> out = new CallableInstance<T>(callable);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Cancellable}.
	 * <p>Note: {@link Monitorable}s are also Cancellables.
	 * @param cancellable the cancellable to use.
	 * @return the new instance.
	 */
	public static <T> Instance<T> spawn(Cancellable<T> cancellable)
	{
		Instance<T> out = new CancellableInstance<T>(cancellable);
		threadPool.execute(out);
		return out;
	}

	private static int relay(InputStream in, OutputStream out) throws IOException
	{
		int total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[8192];
		
		while ((buf = in.read(RELAY_BUFFER)) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
		}
		return total;
	}

	private static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}
	
	/**
	 * A {@link Cancellable} that can listen for changes/progress reported by it.
	 * @param <T> the result type.
	 */
	public static abstract class Monitorable<T> extends Cancellable<T>
	{
		private String message;
		private double currentProgress;
		private double maxProgress;
		private MonitorableListener listener;

		/**
		 * Creates the Monitorable.
		 */
		public Monitorable()
		{
			super();
			this.currentProgress = 0.0;
			this.maxProgress = 0.0;
			this.listener = null;
			this.message = null;
		}

		/**
		 * Sets the listener on this Monitorable.
		 * @param listener the listener to set.
		 */
		public final void setListener(MonitorableListener listener)
		{
			this.listener = listener;
		}

		private static boolean streql(String a, String b)
		{
			return a == b || (a != null && a.equals(b)) || (b != null && b.equals(a));
		}

		/**
		 * Sets the current progress message on this task.
		 * If the provided value is different from the current value, this will alert the listener.
		 * @param message the new current message.
		 */
		public final void setMessage(String message)
		{
			if (!streql(this.message, message))
			{
				this.message = message;
				changed();
			}
		}

		/**
		 * @return the current progress message on this task.
		 */
		public final String getMessage()
		{
			return message;
		}

		/**
		 * Sets the current progress on this task.
		 * If the provided value is different from the current value, this will alert the listener.
		 * @param current the new current progress.
		 */
		public final void setCurrentProgress(double current)
		{
			if (current != this.currentProgress)
			{
				this.currentProgress = current;
				changed();
			}
		}

		/**
		 * @return the current progress on this task.
		 */
		public double getCurrentProgress()
		{
			return currentProgress;
		}
		
		/**
		 * Sets the max progress on this task.
		 * If the provided value is different from the current value, this will alert the listener.
		 * @param max the new maximum progress.
		 */
		public final void setMaxProgress(double max)
		{
			if (max != this.maxProgress)
			{
				this.maxProgress = max;
				changed();
			}
		}

		/**
		 * @return the max progress on this task.
		 */
		public double getMaxProgress() 
		{
			return maxProgress;
		}
		
		/**
		 * Sets the progress on this task.
		 * If any of the provided values are different, this will alert the listener.
		 * @param message the progress message.
		 * @param current the current progress.
		 * @param max the maximum progress.
		 */
		public final void setProgress(String message, double current, double max)
		{
			if (!streql(this.message, message) || current != this.currentProgress || max != this.maxProgress)
			{
				this.message = message;
				this.currentProgress = current;
				this.maxProgress = max;
				changed();
			}
		}

		/**
		 * Sets the progress to "indeterminate."
		 */
		public final void setIndeterminate()
		{
			setProgress(message, 0.0, 0.0);
		}

		/**
		 * Checks if the progress is considered "indeterminate."
		 * This just checks if calculating the progress will result in a value that is not calculable.
		 * @return true if so, false if not.
		 * @see #setIndeterminate()
		 */
		public final boolean isIndeterminate()
		{
			return maxProgress == 0.0;
		}

		// Called on progress change.
		private final void changed()
		{	
			if (listener != null)
				listener.onProgressChange(isIndeterminate(), currentProgress, maxProgress, message);
		}

	}

	/**
	 * A {@link Callable} that can be flagged for cancellation.
	 * The code in {@link #call()} must check to see if it has been cancelled in order to
	 * honor the request, but does not need to guarantee it. 
	 * @param <T> the result type.
	 */
	public static abstract class Cancellable<T> implements Callable<T>
	{
		private boolean isCancelled;

		/**
		 * Creates the Cancellable.
		 */
		public Cancellable()
		{
			isCancelled = false;
		}

		/**
		 * Flags this Cancellable for cancellation.
		 */
		public final void cancel()
		{
			isCancelled = true;
		}

		/**
		 * Checks if this has been flagged for cancellation.
		 * @return true if so, false if not.
		 * @see #cancel()
		 */
		public final boolean isCancelled()
		{
			return isCancelled;
		}

		@Override
		public abstract T call();

	}

	/**
	 * A single instance of a spawned, asynchronous executable task.
	 * Note that this class is a type of {@link RunnableFuture} - this can be used in places
	 * that {@link Future}s can also be used.
	 * @param <T> the result type.
	 */
	public static abstract class Instance<T> implements RunnableFuture<T>
	{
		protected Thread executor;

		private Object waitMutex;
		private boolean done;
		private boolean running;
		private Exception exception;
		private T finishedResult;
	
		private Instance()
		{
			this.executor = null;
			this.waitMutex = new Object();
			this.done = false;
			this.running = false;
			this.exception = null;
			this.finishedResult = null;
		}
	
		@Override
		public void run()
		{
			executor = Thread.currentThread();
			running = true;
			try {
				finishedResult = execute();
			} catch (Exception e) {
				exception = e;
			}
			running = false;
			done = true;
			synchronized (waitMutex)
			{
				waitMutex.notifyAll();
			}
			executor = null;
		}
	
		/**
		 * Checks if this task instance is being worked on by a thread.
		 * @return true if so, false if not.
		 */
		public boolean isRunning()
		{
			return running;
		}
	
		@Override
		public boolean isDone()
		{
			return done;
		}
	
		/**
		 * Makes the calling thread wait indefinitely for this task instance's completion.
		 * @throws InterruptedException if the current thread was interrupted while waiting.
		 */
		public void waitForDone() throws InterruptedException
		{
			while (!isDone())
			{
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
		public void waitForDone(long time, TimeUnit unit) throws InterruptedException
		{
			if (!isDone())
			{
				synchronized (waitMutex)
				{
					unit.timedWait(waitMutex, time);
				}
			}
		}
	
		/**
		 * Gets the exception thrown as a result of this instance completing, making the calling thread wait for its completion.
		 * @return the exception thrown by the encapsulated task, or null if no exception.
		 * @throws ExecutionException if the computation threw an exception.
		 * @throws InterruptedException if the current thread was interrupted while waiting.
		 */
		public Exception getException() throws InterruptedException, ExecutionException
		{
			join();
			return exception;
		}
	
		@Override
		public T get() throws InterruptedException, ExecutionException
		{
			waitForDone();
			if (isCancelled())
				throw new CancellationException("task was cancelled");
			if (getException() != null)
				throw new ExecutionException(getException());
			return finishedResult;
		}
	
		@Override
		public T get(long time, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException
		{
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
		 * Attempts to return the result of this instance, making the calling thread wait for its completion.
		 * <p>This is for convenience - this is like calling {@link #get()}, except it will only throw an
		 * encapsulated {@link RuntimeException} with an exception that {@link #get()} would throw as a cause.
		 * @return the result. Can be null if no result is returned.
		 * @throws RuntimeException if a call to {@link #get()} instead of this would throw an exception.
		 */
		public T result()
		{
			try {
				return get();
			} catch (Exception e) {
				throw new RuntimeException("exception on get", e);
			}
		}
	
		/**
		 * Attempts to return the result of this instance, without waiting for its completion.
		 * <p>If {@link #isDone()} is false, this is guaranteed to return <code>null</code>.
		 * @return the result, or <code>null</code> if not finished.
		 */
		public T resultNonBlocking()
		{
			return finishedResult;
		}
	
		/**
		 * Makes the calling thread wait until this task has finished, returning nothing.
		 */
		public void join()
		{
			try {
				result();
			} catch (Exception e) {
				// Eat exception.
			}
		}
	
		/**
		 * Executes this instance's callable payload.
		 * @return the result from the execution.
		 * @throws Exception for any exception that may occur.
		 */
		protected abstract T execute() throws Exception;
	
	}

	/**
	 * The thread factory used for the Thread Pool.
	 * Makes daemon threads that start with the name <code>"AsyncUtilsThread-"</code>.
	 */
	private static class DefaultThreadFactory implements ThreadFactory
	{
		private static final String THREAD_NAME = "AsyncUtilsThread-";

		private AtomicLong threadId;

		private DefaultThreadFactory()
		{
			threadId = new AtomicLong(0L);
		}

		@Override
		public Thread newThread(Runnable r)
		{
			Thread out = new Thread(r);
			out.setName(THREAD_NAME + threadId.getAndIncrement());
			out.setDaemon(true);
			out.setPriority(Thread.NORM_PRIORITY);
			return out;
		}
		
	}

	/**
	 * Process encapsulation. 
	 */
	private static class ProcessInstance extends Instance<Integer>
	{
		private Process process;
		private boolean cancelled;

		private ProcessInstance(Process process) 
		{
			this.process = process;
			this.cancelled = false;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) 
		{
			process.destroy();
			cancelled = true;
			return true;
		}

		@Override
		public boolean isCancelled() 
		{
			return cancelled;
		}

		@Override
		protected Integer execute() throws Exception 
		{
			process.waitFor();
			return process.exitValue();
		}
		
	}
	
	/**
	 * Cancellable encapsulation. 
	 * @param <T> result type.
	 */
	private static class CancellableInstance<T> extends Instance<T>
	{
		private Cancellable<T> cancellable;
		
		private CancellableInstance(Cancellable<T> cancellable)
		{
			this.cancellable = cancellable;
		}

		@Override
		protected T execute() throws Exception
		{
			return cancellable.call();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			if (isDone())
				return false;
			if (mayInterruptIfRunning)
				executor.interrupt();
			cancellable.cancel();
			return true;
		}

		@Override
		public boolean isCancelled()
		{
			return cancellable.isCancelled();
		}

	}

	/**
	 * Callable encapsulation. 
	 * @param <T> result type.
	 */
	private static class CallableInstance<T> extends Instance<T>
	{
		private Callable<T> callable;

		private CallableInstance(Callable<T> callable)
		{
			this.callable = callable;
		}

		@Override
		protected T execute() throws Exception
		{
			return callable.call();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			// Do nothing.
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

	}

	/**
	 * Runnable encapsulation. 
	 * @param <T> result type.
	 */
	private static class RunnableInstance<T> extends Instance<T>
	{
		private Runnable runnable;
		private T result;

		private RunnableInstance(Runnable runnable, T result)
		{
			this.runnable = runnable;
			this.result = result;
		}

		@Override
		protected T execute() throws Exception
		{
			runnable.run();
			return result;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			// Do nothing.
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

	}

}
