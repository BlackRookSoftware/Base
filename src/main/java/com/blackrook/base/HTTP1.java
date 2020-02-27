package com.blackrook.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * HTTP 1.0/1.1 Functions
 * @author Matthew Tropiano
 */
public final class HTTP1
{
	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	private static final String CRLF = "\r\n";
	private static final Pattern ILLEGAL_TOKEN = Pattern.compile("[0-9\\s\",:;<=>\\?@\\[\\\\\\]\\{\\}]");
	private static final String HEXALPHABET = "0123456789ABCDEF";

	private static final ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(()->new byte[4096]);
	private static final ThreadLocal<SimpleDateFormat> ISO_DATE = ThreadLocal.withInitial(()->new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z"));

	/**
	 * HTTP Version.
	 */
	public enum Version
	{
		HTTP10("HTTP/1.0"),
		HTTP11("HTTP/1.1");
		
		private final String versionString;
		private Version(String versionString)
		{
			this.versionString = versionString;
		}
		
		private String getVersionString() 
		{
			return versionString;
		}
	}
	
	/**
	 * Encodes a string so that it can be input safely into a URL string.
	 * FIXME: Turn string into UTF-8 bytes before conversion.
	 * @param inString the input string.
	 * @return the encoded string.
	 */
	public static String urlEncode(String inString)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			if (!((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a)))
				sb.append(String.format("%%%02x", (short)c));
			else
				sb.append(c);
			i++;
		}
		return sb.toString();
	}

	/**
	 * Decodes a URL-encoded string.
	 * FIXME: Read ASCII bytes, then decode as UTF-8
	 * @param inString the input string.
	 * @return the decoded string.
	 */
	public static String urlDecode(String inString)
	{
		StringBuilder sb = new StringBuilder();
		char[] chars = new char[2];
		int x = 0;
		
		final int STATE_START = 0;
		final int STATE_DECODE = 1;
		int state = STATE_START;
		
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			
			switch (state)
			{
				case STATE_START:
				{
					if (c == '%')
					{
						x = 0;
						state = STATE_DECODE;
					}
					else
						sb.append(c);
				}
				break;
				
				case STATE_DECODE:
				{
					chars[x++] = c;
					if (x == 2)
					{
						int v = 0;
						try {
							v = Integer.parseInt(new String(chars), 16);
							sb.append((char)(v & 0x0ff));
						} catch (NumberFormatException e) {
							sb.append('%').append(chars[0]).append(chars[1]);
						}
						state = STATE_START;
					}
				}
				break;
			}
		}
		
		if (state == STATE_DECODE)
		{
			sb.append('%');
			for (int n = 0; n < x; n++)
				sb.append(chars[n]);
		}
		
		return sb.toString();
	}

	/**
	 * Returns a date string accepted by an HTTP protocol.
	 * @param dateMillis the date in milliseconds since Epoch to format.
	 * @return the output date string.
	 */
	public static String date(long dateMillis)
	{
		return date(new Date(dateMillis));
	}

	/**
	 * Returns a date string accepted by an HTTP protocol.
	 * @param date the date to format.
	 * @return the output date string.
	 */
	public static String date(Date date)
	{
		return ISO_DATE.get().format(date);
	}

	/**
	 * Concatenates a list of strings together, with a joiner string 
	 * added between each (but not before the first, or after the last).
	 * @param joiner the string to add between each string.
	 * @param values the values.
	 * @return the resultant string.
	 */
	public static String join(String joiner, String ... values)
	{
		joiner = String.valueOf(joiner);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			sb.append(values[i]);
			if (i < values.length)
				sb.append(joiner);
		}
		return sb.toString();
	}

	/**
	 * Wraps a string in double quotes, escaping the characters inside if need be.
	 * @param value the value to stringify and wrap.
	 * @return the resultant string.
	 */
	public static String quoted(Object value)
	{
		String val = String.valueOf(value);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < val.length(); i++)
		{
			char c = val.charAt(i);
			if (c == '\\')
				sb.append('\\').append('\\');
			else if (c == '"')
				sb.append('\\').append('"');
			else if (c == '^')
				sb.append('\\').append('^');
			else if (c == '_')
				sb.append('\\').append('_');
			else if (c == '`')
				sb.append('\\').append('`');
			else
				sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Wraps a string in parenthesis, escaping the characters inside if need be.
	 * @param value the value to stringify and wrap.
	 * @return the resultant string.
	 */
	public static String comment(Object value)
	{
		String val = String.valueOf(value);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < val.length(); i++)
		{
			char c = val.charAt(i);
			if (c == '\\')
				sb.append('\\').append('\\');
			else if (c == '(')
				sb.append('\\').append('(');
			else if (c == ')')
				sb.append('\\').append(')');
			else
				sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Returns a <code>key=value</code> style string where the key and value are concatenated with an equals sign <code>=</code>.
	 * The value is wrapped in double-quotes if it contains a character that is illegal in a token.
	 * @param key the key.
	 * @param value the value.
	 * @return the output date string.
	 */
	public static String keyValue(String key, Object value)
	{
		String val = String.valueOf(value);
		if (ILLEGAL_TOKEN.matcher(val).find())
			return key + '=' + quoted(val);
		else
			return key + '=' + val;
	}

	/**
	 * Gets a hexadecimal string for a value.
	 * @param value the input value.
	 * @return the resultant string.
	 */
	public static String hexNumber(int value)
	{
		if (value < 0)
			throw new IllegalArgumentException("value must be 0 or greater");
		return String.format("%X", value);
	}

	/**
	 * A writer that writes HTTP 1.0/1.1 protocol data to an output stream.
	 * This does minimal correcting, as this is a very "bare metal" writer.
	 * It is the user's responsibility to write correct requests/responses; this merely
	 * facilitates the writing.
	 */
	public static class Writer implements AutoCloseable, Flushable
	{
		/** The wrapped output stream. */
		private OutputStream outputStream;
		/** The output stream for writing encoded characters. */
		private OutputStreamWriter encodedOutputStream;
		
		/**
		 * Creates a new HTTP Writer.
		 * @param out the output stream to write to.
		 */
		public Writer(OutputStream out)
		{
			this.outputStream = out;
			this.encodedOutputStream = new OutputStreamWriter(outputStream, ISO_8859_1);
		}
		
		@Override
		public void flush() throws IOException
		{
			encodedOutputStream.flush();
			outputStream.flush();
		}

		@Override
		public void close() throws Exception
		{
			flush();
			outputStream.close();
			encodedOutputStream = null;
		}

		/**
		 * Writes CRLF.
		 * @throws IOException if a write error occurs.
		 */
		public void writeCRLF() throws IOException
		{
			encodedOutputStream.append(CRLF);
		}

		/**
		 * Writes a string of characters to the output stream (plus CRLF).
		 * @param line the line to write.
		 * @throws IOException if a write error occurs.
		 */
		public void writeLine(String line) throws IOException
		{
			encodedOutputStream.append(line);
			writeCRLF();
		}

		/**
		 * Writes the HTTP request header (plus CRLF).
		 * Out of convenience, this safely encodes the URI.
		 * @param method the request method.
		 * @param uri the URI to read. 
		 * @param version the HTTP version.
		 * @throws IOException if a write error occurs.
		 * @see HTTP1#urlEncode(String)
		 */
		public void writeRequestHeader(String method, String uri, Version version) throws IOException
		{
			encodedOutputStream
				.append(method.toUpperCase()).append(' ')
				.append(urlEncode(uri)).append(' ')
				.append(version.getVersionString())
			;
			writeCRLF();
		}
		
		/**
		 * Writes the HTTP response header (plus CRLF).
		 * @param version the HTTP version.
		 * @param statusCode the HTTP status code.
		 * @param statusMessage the status message.
		 * @throws IOException if a write error occurs.
		 */
		public void writeResponseHeader(Version version, int statusCode, String statusMessage) throws IOException
		{
			encodedOutputStream
				.append(version.getVersionString()).append(' ')
				.append(String.valueOf(statusCode)).append(' ')
				.append(statusMessage)
			;
			writeCRLF();
		}
		
		/**
		 * Writes an HTTP header (plus CRLF).
		 * @param header the header name.
		 * @param content the header value. 
		 * @throws IOException if a write error occurs.
		 */
		public void writeHeader(String header, String content) throws IOException
		{
			encodedOutputStream
				.append(header).append(':').append(' ')
				.append(content)
			;
			writeCRLF();
		}
		
		/**
		 * Writes a file's data out to the output stream.
		 * This is for convenience - this will write the file content as-is to the output stream plus a terminal CRLF.
		 * <p><b>NOTE:</b> This does NOT add <code>Content-Length</code> headers or any other information about the data!
		 * @param file the input stream to read from.
		 * @param length the amount of bytes to read.
		 * @return the amount of bytes actually written.
		 * @throws IOException if a write error occurs.
		 */
		public long writeData(File file, long length) throws IOException
		{
			flush(); // flush pending data first
			long out;
			try (FileInputStream fis = new FileInputStream(file))
			{
				out = writeData(fis, length);
			}
			return out;
		}
		
		/**
		 * Writes data out to the output stream.
		 * This is for convenience - this will write the stream content as-is to the output stream plus a terminal CRLF.
		 * The input stream is not closed after this method completes.
		 * <p><b>NOTE:</b> This does NOT add <code>Content-Length</code> headers or any other information about the data!
		 * @param in the input stream to read from.
		 * @param length the amount of bytes to read.
		 * @return the amount of bytes actually written.
		 * @throws IOException if a write error occurs.
		 */
		public long writeData(InputStream in, long length) throws IOException
		{
			flush(); // flush pending data first
			long out = 0L;
			byte[] b = BUFFER.get();
			while (length > 0)
			{
				int lenIntMax = (int)Math.min(Integer.MAX_VALUE, length);
				int buf = in.read(b, 0, Math.min(lenIntMax, b.length));
				if (buf < 0)
					break;
				outputStream.write(b, 0, buf);
				length -= buf;
				out += buf;
			}
			writeCRLF();
			return out;
		}
		
		/**
		 * Writes a file's data out to the output stream.
		 * This is for convenience - this will write the file content as-is to the output stream plus a terminal CRLF.
		 * <p><b>NOTE:</b> This does NOT add <code>Content-Length</code> or <code>Transfer-Encoding</code> headers or any other information about the data!
		 * @param file the input stream to read from.
		 * @param maxChunkLength the maximum amount of bytes per chunk.
		 * @return the amount of bytes actually written.
		 * @throws IOException if a write error occurs.
		 */
		public long writeChunked(File file, int maxChunkLength) throws IOException
		{
			flush(); // flush pending data first
			long out;
			try (FileInputStream fis = new FileInputStream(file))
			{
				out = writeChunked(fis, maxChunkLength);
			}
			return out;
		}
		
		/**
		 * Writes data out to the output stream.
		 * This is for convenience - this will write the stream content as-is to the output stream plus a terminal CRLF.
		 * The input stream is not closed after this method completes.
		 * <p><b>NOTE:</b> This does NOT add <code>Content-Length</code> or <code>Transfer-Encoding</code> headers or any other information about the data!
		 * @param in the input stream to read from.
		 * @param maxChunkLength the maximum amount of bytes per chunk.
		 * @return the amount of bytes actually written.
		 * @throws IOException if a write error occurs.
		 */
		public long writeChunked(InputStream in, int maxChunkLength) throws IOException
		{
			flush(); // flush pending data first
			long out = 0L;
			byte[] b = new byte[maxChunkLength];
			
			int buf;
			while ((buf = in.read(b)) > 0)
			{
				writeLine(hexNumber(buf));
				outputStream.write(b, 0, buf);
				writeCRLF();
			}

			// Terminal chunk.
			writeLine(hexNumber(0));
			writeCRLF();
			return out;
		}

	}
	
	/**
	 * A reader that reads HTTP 1.0/1.1 protocol data from an input stream.
	 * This reads headers blindly into a map and stops right before the content section.
	 */
	public static class Reader implements AutoCloseable
	{
		/** The wrapped input stream. */
		private InputStream inputStream;
		/** The input stream for reading encoded characters. */
		private InputStreamReader decodedInputStream;
		
		/**
		 * Creates a new HTTP Reader.
		 * @param in the input stream to read from.
		 */
		public Reader(InputStream in)
		{
			this.inputStream = in;
			this.decodedInputStream = new InputStreamReader(inputStream, ISO_8859_1);
		}
		
		@Override
		public void close() throws Exception
		{
			inputStream.close();
			decodedInputStream = null;
		}
		
		// TODO: Finish this.
	}
	
}
