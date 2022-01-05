/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Simple IO utility functions.
 * @author Matthew Tropiano
 */
public final class IOUtils
{
	/** The relay buffer size, used by relay(). */
	private static int RELAY_BUFFER_SIZE = 8192;
	/** The input wrapper used by getLine(). */
	private static BufferedReader SYSTEM_IN_READER;

	/** A null outputstream. */
	public static final OutputStream OUTPUTSTREAM_NULL = new OutputStream() 
	{
		@Override
		public void write(int b) throws IOException
		{
			// Do nothing.
		}
		
		@Override
		public void write(byte[] b) throws IOException
		{
			// Do nothing.
		}
	
		@Override
		public void write(byte[] b, int off, int len) throws IOException 
		{
			// Do nothing.
		}
	};

	/** A null inputstream. */
	public static final InputStream INPUTSTREAM_NULL = new InputStream() 
	{
		@Override
		public int read() throws IOException
		{
			return -1;
		}
	};
	
	/** A null reader. */
	public static final Reader READER_NULL = new Reader()
	{
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException 
		{
			return -1;
		}
	
		@Override
		public void close() throws IOException 
		{
			// Do nothing.
		}
	};
	
	/** A null writer. */
	public static final Writer WRITER_NULL = new Writer()
	{
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException 
		{
			// Do nothing.
		}
	
		@Override
		public void flush() throws IOException 
		{
			// Do nothing.
		}
	
		@Override
		public void close() throws IOException 
		{
			// Do nothing.
		}
	};
	
	/** A null printstream. */
	public static final PrintStream PRINTSTREAM_NULL = new PrintStream(OUTPUTSTREAM_NULL);
	
	/** A null print writer. */
	public static final PrintWriter PRINTWRITER_NULL = new PrintWriter(WRITER_NULL);
	
	/** A null file. */
	public static final File NULL_FILE = new File(System.getProperty("os.name").contains("Windows") ? "NUL" : "/dev/null");
	
	private IOUtils() {}
	
	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(in))</code>
	 * @param in the stream to read.
	 * @return an open buffered reader for the provided stream.
	 * @throws IOException if an error occurred opening the stream for reading.
	 * @throws SecurityException if you do not have permission for opening the stream.
	 */
	public static BufferedReader openTextStream(InputStream in) throws IOException
	{
		return new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(new FileInputStream(file)))</code>
	 * @param file the file to open.
	 * @return an open buffered reader for the provided file.
	 * @throws IOException if an error occurred opening the file for reading.
	 * @throws SecurityException if you do not have permission for opening the file.
	 */
	public static BufferedReader openTextFile(File file) throws IOException
	{
		return openTextStream(new FileInputStream(file));
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))</code>
	 * @param filePath the path of the file to open.
	 * @return an open buffered reader for the provided path.
	 * @throws IOException if an error occurred opening the file for reading.
	 * @throws SecurityException if you do not have permission for opening the file.
	 */
	public static BufferedReader openTextFile(String filePath) throws IOException
	{
		return openTextFile(new File(filePath));
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(System.in))</code>
	 * @return an open buffered reader for {@link System#in}.
	 * @throws IOException if an error occurred opening Standard IN.
	 * @throws SecurityException if you do not have permission for opening Standard IN.
	 */
	public static BufferedReader openSystemIn() throws IOException
	{
		return openTextStream(System.in);
	}

	/**
	 * Opens an {@link InputStream} to a resource using the current thread's {@link ClassLoader}.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream openResource(String pathString)
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathString);
	}

	/**
	 * Opens an {@link InputStream} to a resource using a provided ClassLoader.
	 * @param classLoader the provided {@link ClassLoader} to use.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream openResource(ClassLoader classLoader, String pathString)
	{
		return classLoader.getResourceAsStream(pathString);
	}

	/**
	 * Retrieves the ASCII contents of a file.
	 * @param f		the file to use.
	 * @return		a contiguous string (including newline characters) of the file's contents.
	 * @throws FileNotFoundException	if the file cannot be found.
	 * @throws IOException				if the read cannot be done.
	 */
	public static String getASCIIContents(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		String out = getTextualContents(fis, "ASCII");
		fis.close();
		return out;
	}

	/**
	 * Retrieves the textual contents of a file in the system's current encoding.
	 * @param f	the file to use.
	 * @return		a contiguous string (including newline characters) of the file's contents.
	 * @throws IOException	if the read cannot be done.
	 */
	public static String getTextualContents(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		String out = getTextualContents(fis);
		fis.close();
		return out;
	}

	/**
	 * Retrieves the textual contents of a stream in the system's current encoding.
	 * @param in	the input stream to use.
	 * @return		a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException	if the read cannot be done.
	 */
	public static String getTextualContents(InputStream in) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Retrieves the textual contents of a stream.
	 * @param in		the input stream to use.
	 * @param encoding	name of the encoding type.
	 * @return		a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException				if the read cannot be done.
	 */
	public static String getTextualContents(InputStream in, String encoding) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in, encoding));
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Retrieves the binary contents of a file.
	 * @param f		the file to use.
	 * @return		an array of the bytes that make up the file.
	 * @throws FileNotFoundException	if the file cannot be found.
	 * @throws IOException				if the read cannot be done.
	 */
	public static byte[] getBinaryContents(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		byte[] b = getBinaryContents(fis, (int)f.length());
		fis.close();
		return b;
	}

	/**
	 * Retrieves the binary contents of a stream.
	 * @param in	the input stream to use.
	 * @param len	the amount of bytes to read.
	 * @return		an array of len bytes that make up the stream.
	 * @throws IOException				if the read cannot be done.
	 */
	public static byte[] getBinaryContents(InputStream in, int len) throws IOException
	{
		byte[] b = new byte[len];
		in.read(b);
		return b;
	}

	/**
	 * Retrieves the binary contents of a stream until it hits the end of the stream.
	 * @param in	the input stream to use.
	 * @return		an array of len bytes that make up the data in the stream.
	 * @throws IOException	if the read cannot be done.
	 */
	public static byte[] getBinaryContents(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		relay(in, bos);
		return bos.toByteArray();
	}

	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	public static int relay(InputStream in, OutputStream out) throws IOException
	{
		return relay(in, out, RELAY_BUFFER_SIZE, -1);
	}

	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @param bufferSize the buffer size for the I/O. Must be &gt; 0.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	public static int relay(InputStream in, OutputStream out, int bufferSize) throws IOException
	{
		return relay(in, out, bufferSize, -1);
	}

	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @param bufferSize the buffer size for the I/O. Must be &gt; 0.
	 * @param maxLength the maximum amount of bytes to relay, or a value &lt; 0 for no max.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	public static int relay(InputStream in, OutputStream out, int bufferSize, int maxLength) throws IOException
	{
		int total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[bufferSize];
		
		while ((buf = in.read(RELAY_BUFFER, 0, Math.min(maxLength < 0 ? Integer.MAX_VALUE : maxLength, bufferSize))) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
			if (maxLength >= 0)
				maxLength -= buf;
		}
		return total;
	}

	/**
	 * Sets the size of the buffer in bytes for {@link #relay(InputStream, OutputStream)}.
	 * Although you may not encounter this problem, it would be unwise to set this during a call to relay().
	 * Size cannot be 0 or less.
	 * @param size the size of the relay buffer. 
	 */
	public static void setRelayBufferSize(int size)
	{
		if (size <= 0)
			throw new IllegalArgumentException("size is 0 or less.");
		RELAY_BUFFER_SIZE = size;
	}

	/**
	 * @return the size of the relay buffer for {@link #relay(InputStream, OutputStream)} in bytes.
	 */
	public static int getRelayBufferSize()
	{
		return RELAY_BUFFER_SIZE;
	}

	/**
	 * Reads a line from standard in; throws a RuntimeException
	 * if something absolutely serious happens. Should be used
	 * just for convenience.
	 * @return a single line read from Standard In.
	 * @see #openSystemIn()
	 * @see BufferedReader#readLine()
	 */
	public static String getLine()
	{
		String out = null;
		try {
			if (SYSTEM_IN_READER == null)
				SYSTEM_IN_READER = openSystemIn();
			out = SYSTEM_IN_READER.readLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return out;
	}

	/**
	 * Attempts to close a {@link Closeable} object.
	 * If the object is null, this does nothing.
	 * @param c the reference to the closeable object.
	 */
	public static void close(Closeable c)
	{
		if (c == null) return;
		try { c.close(); } catch (IOException e){}
	}

	/**
	 * Attempts to close an {@link AutoCloseable} object.
	 * If the object is null, this does nothing.
	 * @param c the reference to the AutoCloseable object.
	 */
	public static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}

	/**
	 * BufferedReader to Writer stream thread.
	 * Transfers characters buffered at one line at a time until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	public static class LineReaderToWriterThread extends Thread
	{
		protected BufferedReader sourceReader;
		protected PrintWriter targetPrintWriter;
		protected PrintStream targetPrintStream;
		
		private Throwable exception;
		private long totalCharacters;
		
		private LineReaderToWriterThread(BufferedReader reader)
		{
			super("LineReaderToWriterThread");
			this.sourceReader = reader;
			this.exception = null;
			this.totalCharacters = 0L;
		}
		
		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the Reader to read from (wrapped as a BufferedReader).
		 * @param writer the Writer to write to.
		 */
		public LineReaderToWriterThread(Reader reader, Writer writer)
		{
			this(new BufferedReader(reader), new PrintWriter(writer));
		}

		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param writer the Writer to write to.
		 */
		public LineReaderToWriterThread(BufferedReader reader, PrintWriter writer)
		{
			this(reader);
			this.targetPrintWriter = writer;
		}

		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param stream the PrintStream to write to.
		 */
		public LineReaderToWriterThread(BufferedReader reader, PrintStream stream)
		{
			this(reader);
			this.targetPrintStream = stream;
		}

		@Override
		public final void run() 
		{
			String line;
			try {
				while ((line = sourceReader.readLine()) != null)
				{
					if (targetPrintWriter != null)
					{
						targetPrintWriter.append(line).append('\n').flush();
					}
					else
					{
						targetPrintStream.println(line);
						targetPrintStream.flush();
					}
					totalCharacters += line.length() + 1;
				}
			} catch (Throwable e) {
				exception = e;
			} finally {
				afterClose();
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
		/**
		 * @return the exception that occurred, if any.
		 */
		public Throwable getException() 
		{
			return exception;
		}
		
		/**
		 * @return the total amount of characters moved.
		 */
		public long getTotalCharacters() 
		{
			return totalCharacters;
		}
		
	}
	
	/**
	 * Reader to Writer stream thread.
	 * Transfers characters until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	public static class ReaderToWriterThread extends Thread
	{
		protected Reader sourceReader;
		protected Writer targetWriter;
		
		private Throwable exception;
		private long totalCharacters;
		
		/**
		 * Creates a new thread that, when started, transfers characters until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the Reader to read from.
		 * @param writer the Writer to write to.
		 */
		public ReaderToWriterThread(Reader reader, Writer writer)
		{
			super("ReaderToWriterThread");
			this.sourceReader = reader;
			this.targetWriter = writer;
			this.exception = null;
			this.totalCharacters = 0L;
		}

		@Override
		public final void run() 
		{
			int buf;
			char[] buffer = new char[8192]; 
			try {
				while ((buf = sourceReader.read(buffer)) > 0)
				{
					targetWriter.write(buffer, 0, buf);
					targetWriter.flush();
					totalCharacters += buf;
				}
			} catch (Throwable e) {
				exception = e;
			} finally {
				afterClose();
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
		/**
		 * @return the exception that occurred, if any.
		 */
		public Throwable getException() 
		{
			return exception;
		}
		
		/**
		 * @return the total amount of characters moved.
		 */
		public long getTotalCharacters() 
		{
			return totalCharacters;
		}
		
	}
	
	/**
	 * Input to Output stream thread.
	 * Transfers until the source stream is closed.
	 * <p> The thread terminates if the input stream is closed. The target output stream is not closed.
	 */
	public static class InputToOutputStreamThread extends Thread
	{
		protected InputStream sourceStream;
		protected OutputStream targetStream;
		
		private Throwable exception;
		private long totalBytes;
		
		/**
		 * Creates a new thread that, when started, transfers bytes until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param sourceStream the InputStream to read from. 
		 * @param targetStream the OutputStream to write to.
		 */
		public InputToOutputStreamThread(InputStream sourceStream, OutputStream targetStream)
		{
			super("InputToOutputStreamThread");
			this.sourceStream = sourceStream;
			this.targetStream = targetStream;
			this.exception = null;
			this.totalBytes = 0L;
		}
		
		@Override
		public final void run() 
		{
			int buf;
			byte[] buffer = new byte[8192]; 
			try {
				while ((buf = sourceStream.read(buffer)) > 0)
				{
					targetStream.write(buffer, 0, buf);
					targetStream.flush();
					totalBytes += buf;
				}
			} catch (Throwable e) {
				exception = e;
			} finally {
				afterClose();
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
		/**
		 * @return the exception that occurred, if any.
		 */
		public Throwable getException() 
		{
			return exception;
		}
		
		/**
		 * @return the total amount of bytes moved.
		 */
		public long getTotalBytes() 
		{
			return totalBytes;
		}
		
	}

	/**
	 * Process wrapper. 
	 */
	public static class ProcessWrapper
	{
		private Process process;
		private Thread stdOutThread;
		private Thread stdErrThread;
		private Thread stdInThread;
		private boolean done;
		
		private ProcessWrapper(Process process)
		{
			this.process = process;
			this.stdOutThread = null;
			this.stdErrThread = null;
			this.stdInThread = null;
			this.done = false;
		}
		
		/**
		 * Creates a new process wrapper.
		 * @param process the created process.
		 * @return a new wrapper.
		 */
		public static ProcessWrapper create(Process process)
		{
			return new ProcessWrapper(process);
		}
		
		private void checkNotDone()
		{
			if (done)
				throw new IllegalStateException("Process already finished.");
		}
		
		private void checkStandardOut()
		{
			if (stdOutThread != null)
				throw new IllegalStateException("STDOUT redirect already added.");
		}
		
		private void checkStandardError()
		{
			if (stdErrThread != null)
				throw new IllegalStateException("STDERR redirect already added.");
		}
		
		private void checkStandardIn()
		{
			if (stdInThread != null)
				throw new IllegalStateException("STDIN redirect already added.");
		}
	
		/**
		 * Redirects standard out.
		 * @param newOut the new output stream to redirect STDOUT to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stdout(OutputStream newOut)
		{
			checkNotDone();
			checkStandardOut();
			newOut = newOut == null ? IOUtils.OUTPUTSTREAM_NULL : newOut;
			(stdOutThread = new InputToOutputStreamThread(process.getInputStream(), newOut)).start();
			return this;
		}
		
		/**
		 * Redirects standard out.
		 * @param newOut the new writer to redirect STDOUT to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stdout(Writer newOut)
		{
			checkNotDone();
			checkStandardOut();
			newOut = newOut == null ? IOUtils.WRITER_NULL : newOut;
			(stdOutThread = new ReaderToWriterThread(new InputStreamReader(process.getInputStream()), newOut)).start();
			return this;
		}
		
		/**
		 * Redirects standard out.
		 * @param newOut the new print stream to redirect STDOUT to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stdout(PrintStream newOut)
		{
			checkNotDone();
			checkStandardOut();
			newOut = newOut == null ? IOUtils.PRINTSTREAM_NULL : newOut;
			(stdOutThread = new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getInputStream())), newOut)).start();
			return this;
		}
		
		/**
		 * Redirects standard out.
		 * @param encoding the output encoding.
		 * @param newOut the new writer to redirect STDOUT to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stdout(Charset encoding, Writer newOut)
		{
			checkNotDone();
			checkStandardOut();
			newOut = newOut == null ? IOUtils.WRITER_NULL : newOut;
			(stdOutThread = new ReaderToWriterThread(new InputStreamReader(process.getInputStream(), encoding), newOut)).start();
			return this;
		}
		
		/**
		 * Redirects standard out.
		 * @param encoding the output encoding.
		 * @param newOut the new print stream to redirect STDOUT to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stdout(Charset encoding, PrintStream newOut)
		{
			checkNotDone();
			checkStandardOut();
			newOut = newOut == null ? IOUtils.PRINTSTREAM_NULL : newOut;
			(stdOutThread = new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getInputStream(), encoding)), newOut)).start();
			return this;
		}
		
		/**
		 * Redirects standard error.
		 * @param newErr the new output stream to redirect STDERR to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stderr(OutputStream newErr)
		{
			checkNotDone();
			checkStandardError();
			newErr = newErr == null ? IOUtils.OUTPUTSTREAM_NULL : newErr;
			(stdErrThread = new InputToOutputStreamThread(process.getErrorStream(), newErr)).start();
			return this;
		}
		
		/**
		 * Redirects standard error.
		 * @param newErr the new writer to redirect STDERR to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stderr(Writer newErr)
		{
			checkNotDone();
			checkStandardError();
			newErr = newErr == null ? IOUtils.WRITER_NULL : newErr;
			(stdErrThread = new ReaderToWriterThread(new InputStreamReader(process.getErrorStream()), newErr)).start();
			return this;
		}
		
		/**
		 * Redirects standard error.
		 * @param newErr the new print stream to redirect STDERR to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stderr(PrintStream newErr)
		{
			checkNotDone();
			checkStandardError();
			newErr = newErr == null ? IOUtils.PRINTSTREAM_NULL : newErr;
			(stdErrThread = new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getErrorStream())), newErr)).start();
			return this;
		}
		
		/**
		 * Redirects standard error.
		 * @param encoding the output encoding.
		 * @param newErr the new writer to redirect STDERR to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stderr(Charset encoding, Writer newErr)
		{
			checkNotDone();
			checkStandardError();
			newErr = newErr == null ? IOUtils.WRITER_NULL : newErr;
			(stdErrThread = new ReaderToWriterThread(new InputStreamReader(process.getErrorStream(), encoding), newErr)).start();
			return this;
		}
		
		/**
		 * Redirects standard error.
		 * @param encoding the output encoding.
		 * @param newErr the new print stream to redirect STDERR to.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stderr(Charset encoding, PrintStream newErr)
		{
			checkNotDone();
			checkStandardError();
			newErr = newErr == null ? IOUtils.PRINTSTREAM_NULL : newErr;
			(stdErrThread = new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getErrorStream(), encoding)), newErr)).start();
			return this;
		}
		
		/**
		 * Redirects standard in.
		 * @param newIn the new input stream to redirect STDIN from.
		 * @return this, for chaining.
		 */
		public ProcessWrapper stdin(InputStream newIn)
		{
			checkNotDone();
			checkStandardIn();
			newIn = newIn == null ? IOUtils.INPUTSTREAM_NULL : newIn;
			(stdInThread = new InputToOutputStreamThread(newIn, process.getOutputStream())).start();
			return this;
		}
		
		/**
		 * Gets a killswitch Runnable.
		 * If {@link Runnable#run()} is called on it, the process will terminate.
		 * @return a runnable that can terminate the underlying process.
		 */
		public Runnable getKillSwitch()
		{
			return () -> {process.destroy();};
		}
		
		/**
		 * Waits for the wrapped process to exit as well as associated stream piping threads.
		 * @return the process return code.
		 * @throws InterruptedException if the current thread is interrupted by another 
		 * 		thread while it is waiting, then the wait is ended and an InterruptedException is thrown.
		 */
		public int waitFor() throws InterruptedException
		{
			int out = process.waitFor();
			if (stdOutThread != null)
			{
				stdOutThread.join();
				stdOutThread = null;
			}
			if (stdErrThread != null)
			{
				stdErrThread.join();
				stdErrThread = null;
			}
			if (stdInThread != null)
			{
				stdInThread.join();
				stdInThread = null;
			}
			done = true;
			return out;
		}
		
	}
	
}
