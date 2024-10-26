/*******************************************************************************
 * Copyright (c) 2024 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * CSV Reader class.
 * Reads CSV files in given encodings.
 * Methods are NOT thread-safe.
 * @author Matthew Tropiano
 */
public class CSVReader implements AutoCloseable, Iterable<List<String>>
{
	/** The underlying reader for CSV data. */
	private BufferedReader reader;
	
	/**
	 * Creates a new CSV reader by opening a file.
	 * Assumes system encoding.
	 * @param fileName the file to open.
	 * @throws FileNotFoundException if the file could not be found.
	 */
	public CSVReader(String fileName) throws FileNotFoundException
	{
		this(new File(fileName), Charset.defaultCharset());
	}
	
	/**
	 * Creates a new CSV reader by opening a file.
	 * Assumes system encoding.
	 * @param file the file to open.
	 * @throws FileNotFoundException if the file could not be found.
	 */
	public CSVReader(File file) throws FileNotFoundException
	{
		this(new FileInputStream(file), Charset.defaultCharset());
	}
	
	/**
	 * Creates a new CSV reader by opening a file.
	 * @param file the file to open.
	 * @param charset the file encoding charset.
	 * @throws FileNotFoundException if the file could not be found.
	 */
	public CSVReader(File file, Charset charset) throws FileNotFoundException
	{
		this(new FileInputStream(file), charset);
	}

	/**
	 * Creates a new CSV reader by reading from an input stream.
	 * The stream is read until the end of the data is reached.
	 * Assumes system encoding.
	 * @param in the stream to read from.
	 */
	public CSVReader(InputStream in)
	{
		this(in, Charset.defaultCharset());
	}

	/**
	 * Creates a new CSV reader by reading from an input stream.
	 * The stream is read until the end of the data is reached.
	 * @param in the stream to read from.
	 * @param charset the input encoding charset.
	 */
	public CSVReader(InputStream in, Charset charset)
	{
		this(new InputStreamReader(in, charset));
	}
	
	/**
	 * Creates a new CSV reader by reading from an open reader.
	 * The stream is read until the end of the data is reached.
	 * @param reader the reader to read from.
	 */
	public CSVReader(Reader reader)
	{
		this.reader = new BufferedReader(reader);
	}
	
	/**
	 * Parses a single row and returns it as a list of tokens.
	 * @return the next row, or null if end of stream reached.
	 * @throws IOException if the data cannot be read.
	 */
	public List<String> nextRow() throws IOException
	{
		String line = reader.readLine();
		if (line == null)
			return null;
		
		final int STATE_READ = 0;
		final int STATE_IN_QUOTED = 1;
		final int STATE_IN_QUOTED_MAYBE_END = 2;
		int state = STATE_READ;
		
		List<String> out = new ArrayList<>(24);
		StringBuilder tokenBuf = new StringBuilder(256);
		
		for (int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			
			switch (state)
			{
				case STATE_READ:
				{
					if (c == '"')
					{
						state = STATE_IN_QUOTED;
					}
					else if (c == ',')
					{
						out.add(tokenBuf.toString());
						tokenBuf.delete(0, tokenBuf.length());
					}
					else
					{
						tokenBuf.append(c);
					}
				}
				break;
				
				case STATE_IN_QUOTED:
				{
					if (c == '"')
					{
						state = STATE_IN_QUOTED_MAYBE_END;
					}
					else
					{
						tokenBuf.append(c);
					}
				}
				break;
				
				case STATE_IN_QUOTED_MAYBE_END:
				{
					if (c == '"')
					{
						tokenBuf.append('"');
						state = STATE_IN_QUOTED;
					}
					else
					{
						state = STATE_READ;
					}
				}
				break;
			}
		}
		
		out.add(tokenBuf.toString());
		
		return out;
	}
	
	@Override
	public void close() throws IOException
	{
		reader.close();
	}

	@Override
	public Iterator<List<String>> iterator() 
	{
		return new CSVRowIterator(this);
	}
	
	private static class CSVRowIterator implements Iterator<List<String>>
	{
		private final CSVReader csvReader;
		private List<String> next;
		private boolean readLatch;
		
		private CSVRowIterator(CSVReader reader)
		{
			this.csvReader = reader;
			this.next = null;
			this.readLatch = false;
		}

		@Override
		public boolean hasNext()
		{
			if (readLatch)
				return next != null;
			
			readLatch = true;

			try {
				next = csvReader.nextRow();
			} catch (IOException e) {
				// Eat exception and fall through
			}
			
			return next != null;
		}

		@Override
		public List<String> next() 
		{
			List<String> out = next;
			readLatch = false;
			next = null;
			return out;
		}
		
	}
	
}
