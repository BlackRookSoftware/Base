package com.blackrook.base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * HTTP 1.0/1.1 Functions
 * @author Matthew Tropiano
 */
public final class HTTP1
{
	private static final String MALFORMED_NAME = "---MALFORMED---";
	private static final Charset ASCII = Charset.forName("ASCII");
	private static final CharsetEncoder ASCIIENC = ASCII.newEncoder();
	//private static final CharsetDecoder ASCIIDEC = ASCII.newDecoder();
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String HEXALPHABET = "0123456789ABCDEF";
	private static final byte[] CRLF = "\r\n".getBytes(ASCII);

	private static final char[] CHARS_ILLEGAL_TOKEN = apply("0123456789 \",:;,=>?@[]{}\\".toCharArray(), (a)->Arrays.sort(a));
	private static final char[] CHARS_METHOD = apply("ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), (a)->Arrays.sort(a));
	private static final char[] CHARS_URI = apply("!#$%&/;=?~abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), (a)->Arrays.sort(a));
	private static final char[] CHARS_VERSION = apply("10./HTP".toCharArray(), (a)->Arrays.sort(a));
	private static final char[] CHARS_STATUS_CODE = apply("0123456789".toCharArray(), (a)->Arrays.sort(a));
	private static final char[] CHARS_HEADER_NAME = apply("0123456789-ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(), (a)->Arrays.sort(a));

	private static final ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(()->new byte[4096]);
	private static final ThreadLocal<ByteBuffer> BYTEBUFFER = ThreadLocal.withInitial(()->ByteBuffer.allocate(2048));
	private static final ThreadLocal<CharBuffer> CHARBUFFER = ThreadLocal.withInitial(()->CharBuffer.allocate(2048));
	private static final ThreadLocal<StringBuilder> STRINGBUILDER = ThreadLocal.withInitial(()->new StringBuilder(128));
	private static final ThreadLocal<SimpleDateFormat> ISO_DATE = ThreadLocal.withInitial(()->
	{
		SimpleDateFormat out = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		out.setTimeZone(TimeZone.getTimeZone("GMT"));
		return out;
	});

	/**
	 * Kotlin-esque "apply" functionality facilitator.
	 */
	private static <T> T apply(T input, Consumer<T> applier)
	{
		applier.accept(input);
		return input;
	}
	
	/**
	 * HTTP Version.
	 */
	public enum Version
	{
		HTTP10("HTTP/1.0"),
		HTTP11("HTTP/1.1"),
		UNKNOWN("UNKNOWN");

		private static final Map<String, Version> VERSION_STRING_MAP = apply(new HashMap<>(1, 1f), (m)->
		{
			for (Version v : values())
				m.put(v.getVersionString(), v);
		});
		
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
	 * Request header.
	 */
	public static class RequestHeader
	{
		/**
		 * The RequestHeader object returned when a malformed HTTP request header is read.
		 */
		public static final RequestHeader MALFORMED = apply(new RequestHeader(), (h)->
		{
			h.method = MALFORMED_NAME;
			h.uri = null;
			h.version = null;
		});
		
		private String method;
		private String uri;
		private Version version;
		
		/**
		 * @return the request method.
		 */
		public String getMethod()
		{
			return method;
		}
		
		/**
		 * @return the request URI.
		 */
		public String getURI() 
		{
			return uri;
		}
		
		/**
		 * @return the request HTTP version.
		 */
		public Version getVersion() 
		{
			return version;
		}
		
		/**
		 * Tests if the header is a malformed header.
		 * @return true if so, false if not.
		 */
		public boolean isMalformed()
		{
			return this == MALFORMED;
		}

		@Override
		public String toString() 
		{
			return method + " " + uri + " " + version.getVersionString();
		}
	}
	
	/**
	 * Response header.
	 */
	public static class ResponseHeader
	{
		/**
		 * The RequestHeader object returned when a malformed HTTP request header is read.
		 */
		public static final ResponseHeader MALFORMED = apply(new ResponseHeader(), (h)->
		{
			h.statusDescription = MALFORMED_NAME;
			h.version = null;
			h.statusCode = 0;
		});
		
		private Version version;
		private int statusCode;
		private String statusDescription;
		
		/**
		 * @return the request HTTP version.
		 */
		public Version getVersion() 
		{
			return version;
		}

		/**
		 * @return the HTTP status code.
		 */
		public int getStatusCode() 
		{
			return statusCode;
		}

		/**
		 * @return the status description.
		 */
		public String getStatusDescription()
		{
			return statusDescription;
		}
		
		/**
		 * Tests if the header is a malformed header.
		 * @return true if so, false if not.
		 */
		public boolean isMalformed()
		{
			return this == MALFORMED;
		}
		
		@Override
		public String toString() 
		{
			return version.getVersionString() + " " + statusCode + " " + statusDescription;
		}
	}
	
	/**
	 * An HTTP Header read from a request/response. 
	 */
	public static class Header
	{
		/**
		 * The Header object returned when a malformed HTTP header is read.
		 */
		public static final Header MALFORMED = apply(new Header(),(h)->
		{
			h.name = MALFORMED_NAME;
			h.value = null;
		});
		
		private String name;
		private String value;
		
		/**
		 * @return the header name, as read from the transmission.
		 */
		public String getName() 
		{
			return name;
		}

		/**
		 * @return the header value, as read from the transmission.
		 */
		public String getValue() 
		{
			return value;
		}
		
		/**
		 * Tests if the header is an expected name.
		 * This is a more lenient check in accordance with
		 * how header names are verified (case-insensitively).
		 * @param name the name to test.
		 * @return true if matched, false if not.
		 */
		public boolean is(String name)
		{
			return this.name.equalsIgnoreCase(name);
		}
		
		/**
		 * Tests if the header is a malformed header.
		 * @return true if so, false if not.
		 */
		public boolean isMalformed()
		{
			return this == MALFORMED;
		}
		
		@Override
		public String toString() 
		{
			return name + ": " + value;
		}
	}
	
	/**
	 * Encodes a string so that it can be input safely into a URL string.
	 * FIXME: This is hilariously broken.
	 * @param inString the input string.
	 * @return the encoded string.
	 */
	public static String uriEncode(String inString)
	{
		byte[] inBytes = inString.getBytes(UTF8);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inBytes.length; i++)
		{
			byte b = inBytes[i];
			if (!((b >= 0x30 && b <= 0x39) || (b >= 0x41 && b <= 0x5a) || (b >= 0x61 && b <= 0x7a)))
				sb.append(String.format("%%%02x", 0x0ff & b));
			else
				sb.append((char)(0x0ff & b));
			i++;
		}
		return sb.toString();
	}

	/**
	 * Decodes a URL-encoded string.
	 * FIXME: This is untested.
	 * @param inString the input string.
	 * @return the decoded string.
	 */
	public static String uriDecode(String inString)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
		final int STATE_START = 0;
		final int STATE_DECODE = 1;
		int state = STATE_START;
		
		int x = 0;
		StringBuilder chars = new StringBuilder(2);
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			switch (state)
			{
				case STATE_START:
				{
					if (c == '%')
					{
						state = STATE_DECODE;
						chars.delete(0, 2);
						x = 0;
					}
					else
					{
						bos.write(0x0ff & c);
					}
				}
				break;
				
				case STATE_DECODE:
				{
					if (x == 0)
					{
						chars.append(c);
						x++;
					}
					else if (x == 1)
					{
						try {
							bos.write(Integer.parseInt(chars, 0, 2, 16));
						} catch (NumberFormatException e) {
							bos.write('%');
							bos.write(chars.charAt(0));
							bos.write(chars.charAt(1));
						}
						state = STATE_START;
						x = 0;
					}
				}
				break;
			}
		}
		
		if (state == STATE_DECODE)
		{
			bos.write('%');
			for (int n = 0; n < x; n++)
				bos.write(chars.charAt(x));
		}
		
		return new String(bos.toByteArray(), UTF8);
	}

	/**
	 * @return the date formatter used for parsing dates.
	 */
	public static SimpleDateFormat dateFormat()
	{
		return ISO_DATE.get();
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
		return dateFormat().format(date);
	}

	/**
	 * Parses a date.
	 * @param dateString the date string to parse.
	 * @return the output date string, or null if not parseable.
	 * @see #dateFormat()
	 */
	public static Date date(String dateString)
	{
		try {
			return dateFormat().parse(dateString);
		} catch (ParseException e) {
			return null;
		}
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
			if (i < values.length - 1)
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
		for (int i = 0; i < val.length(); i++)
			if (Arrays.binarySearch(CHARS_ILLEGAL_TOKEN, val.charAt(i)) >= 0)
				return key + '=' + quoted(val);
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
		
		/**
		 * Creates a new HTTP Writer.
		 * @param out the output stream to write to.
		 */
		public Writer(OutputStream out)
		{
			this.outputStream = out;
		}
		
		@Override
		public void flush() throws IOException
		{
			outputStream.flush();
		}

		@Override
		public void close() throws IOException
		{
			flush();
			outputStream.close();
		}

		/**
		 * Writes CRLF.
		 * @throws IOException if a write error occurs.
		 */
		public void writeCRLF() throws IOException
		{
			outputStream.write(CRLF);
		}

		/**
		 * Writes a string of characters to the output stream (plus CRLF).
		 * @param line the line to write.
		 * @throws IOException if a write error occurs.
		 */
		public void writeLine(String line) throws IOException
		{
			CharBuffer cb = CHARBUFFER.get();
			ByteBuffer bb = BYTEBUFFER.get();
			try {
				cb.append(line).flip();
				ASCIIENC.encode(cb, bb, true);
				bb.flip();
				while (bb.hasRemaining())
					outputStream.write(bb.get());
			} finally {
				cb.clear();
				bb.clear();
			}
			writeCRLF();
		}

		/**
		 * Writes the HTTP request header (plus CRLF).
		 * Out of convenience, this safely encodes the URI.
		 * @param method the request method.
		 * @param uri the URI to read. 
		 * @param version the HTTP version.
		 * @throws IOException if a write error occurs.
		 * @see HTTP1#uriEncode(String)
		 */
		public void writeRequestHeader(String method, String uri, Version version) throws IOException
		{
			CharBuffer cb = CHARBUFFER.get();
			ByteBuffer bb = BYTEBUFFER.get();
			try {
				cb.append(method.toUpperCase()).append(' ')
					.append(uriEncode(uri)).append(' ')
					.append(version.getVersionString())
					.flip();
				ASCIIENC.encode(cb, bb, true);
				bb.flip();
				while (bb.hasRemaining())
					outputStream.write(bb.get());
			} finally {
				cb.clear();
				bb.clear();
			}
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
			CharBuffer cb = CHARBUFFER.get();
			ByteBuffer bb = BYTEBUFFER.get();
			try {
				cb.append(version.getVersionString()).append(' ')
					.append(String.valueOf(statusCode)).append(' ')
					.append(statusMessage)
					.flip();
				ASCIIENC.encode(cb, bb, true);
				bb.flip();
				while (bb.hasRemaining())
					outputStream.write(bb.get());
			} finally {
				cb.clear();
				bb.clear();
			}
			writeCRLF();
		}
		
		/**
		 * Writes an HTTP header (plus CRLF).
		 * @param name the header name.
		 * @param value the header value (null for blank).
		 * @throws IOException if a write error occurs.
		 */
		public void writeHeader(String name, Object value) throws IOException
		{
			writeHeader(name, value != null ? String.valueOf(value) : "");
		}
		
		/**
		 * Writes an HTTP header (plus CRLF).
		 * @param name the header name.
		 * @param value the header value. 
		 * @throws IOException if a write error occurs.
		 */
		public void writeHeader(String name, String value) throws IOException
		{
			CharBuffer cb = CHARBUFFER.get();
			ByteBuffer bb = BYTEBUFFER.get();
			try {
				cb.append(name).append(':').append(' ')
					.append(value)
					.flip();
				ASCIIENC.encode(cb, bb, true);
				bb.flip();
				while (bb.hasRemaining())
					outputStream.write(bb.get());
			} finally {
				cb.clear();
				bb.clear();
			}
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
		
		/**
		 * Creates a new HTTP Reader.
		 * @param in the input stream to read from.
		 */
		public Reader(InputStream in)
		{
			this.inputStream = in;
		}
		
		@Override
		public void close() throws IOException
		{
			inputStream.close();
		}
		
		/**
		 * Attempts to read an HTTP request header - method, URI, Protocol version, and CRLF.
		 * @return the read header or {@link RequestHeader#MALFORMED} if malformed.
		 * @throws IOException if a read error occurs.
		 */
		public RequestHeader readRequestHeader() throws IOException
		{
			final int STATE_METHOD = 0;
			final int STATE_URI = 1;
			final int STATE_VERSION = 2;
			final int STATE_CR = 3;
			int state = STATE_METHOD;
			
			String method = null;
			String uri = null;
			Version version = null;
			
			boolean keepGoing = true;
			StringBuilder sb = STRINGBUILDER.get();
			try 
			{
				int r;
				char c;
				while (keepGoing && (r = inputStream.read()) >= 0)
				{
					c = (char)r;
					switch (state)
					{
						case STATE_METHOD:
						{
							if (Arrays.binarySearch(CHARS_METHOD, c) >= 0)
							{
								sb.append(c);
							}
							else if (c == ' ')
							{
								method = sb.toString();
								sb.delete(0, sb.length());
								state = STATE_URI;
							}
							else
							{
								return RequestHeader.MALFORMED;
							}
						}
						break;

						case STATE_URI:
						{
							if (Arrays.binarySearch(CHARS_URI, c) >= 0)
							{
								sb.append(c);
							}
							else if (c == ' ')
							{
								uri = uriDecode(sb.toString());
								sb.delete(0, sb.length());
								state = STATE_VERSION;
							}
							else
							{
								return RequestHeader.MALFORMED;
							}
						}
						break;
						
						case STATE_VERSION:
						{
							if (Arrays.binarySearch(CHARS_VERSION, c) >= 0)
							{
								sb.append(c);
							}
							else if (c == '\r')
							{
								version = Version.VERSION_STRING_MAP.get(sb.toString());
								sb.delete(0, sb.length());
								if (version == null)
									version = Version.UNKNOWN;
								state = STATE_CR;
							}
							else
							{
								return RequestHeader.MALFORMED;
							}							
						}
						break;
						
						case STATE_CR:
						{
							if (c == '\n')
							{
								keepGoing = false;
							}
							else
							{
								return RequestHeader.MALFORMED;
							}							
						}
						break;
					}
				}
			} 
			finally 
			{
				sb.delete(0, sb.length());
			}
			
			// if true, EOS happened.
			if (keepGoing)
				return RequestHeader.MALFORMED;
			
			RequestHeader out = new RequestHeader();
			out.method = method;
			out.uri = uri;
			out.version = version;
			return out;
		}
		
		/**
		 * Attempts to read an HTTP response header - Protocol version, status code, status description, and CRLF.
		 * @return the read header or {@link ResponseHeader#MALFORMED} if malformed.
		 * @throws IOException if a read error occurs.
		 */
		public ResponseHeader readResponseHeader() throws IOException
		{
			final int STATE_VERSION = 0;
			final int STATE_CODE = 1;
			final int STATE_DESCRIPTION = 2;
			final int STATE_CR = 3;
			int state = STATE_VERSION;
			
			Version version = null;
			int statusCode = 0;
			String statusDescription = null;
			
			boolean keepGoing = true;
			StringBuilder sb = STRINGBUILDER.get();
			try 
			{
				int r;
				char c;
				while (keepGoing && (r = inputStream.read()) >= 0)
				{
					c = (char)r;
					switch (state)
					{
						case STATE_VERSION:
						{
							if (Arrays.binarySearch(CHARS_VERSION, c) >= 0)
							{
								sb.append(c);
							}
							else if (c == ' ')
							{
								version = Version.VERSION_STRING_MAP.get(sb.toString());
								sb.delete(0, sb.length());
								if (version == null)
									version = Version.UNKNOWN;
								state = STATE_CODE;
							}
							else
							{
								return ResponseHeader.MALFORMED;
							}							
						}
						break;
						
						case STATE_CODE:
						{
							if (Arrays.binarySearch(CHARS_STATUS_CODE, c) >= 0)
							{
								statusCode = (statusCode * 10) + (c - '0');
							}
							else if (c == ' ')
							{
								state = STATE_DESCRIPTION;
							}
							else
							{
								return ResponseHeader.MALFORMED;
							}
						}
						break;

						case STATE_DESCRIPTION:
						{
							if (c == '\r')
							{
								statusDescription = sb.toString();
								sb.delete(0, sb.length());
								state = STATE_CR;
							}
							else
							{
								sb.append(c);
							}
						}
						break;

						case STATE_CR:
						{
							if (c == '\n')
							{
								keepGoing = false;
							}
							else
							{
								return ResponseHeader.MALFORMED;
							}							
						}
						break;
					}
				}
			} 
			finally 
			{
				sb.delete(0, sb.length());
			}

			// if true, EOS happened.
			if (keepGoing)
				return ResponseHeader.MALFORMED;
			
			ResponseHeader out = new ResponseHeader();
			out.version = version;
			out.statusCode = statusCode;
			out.statusDescription = statusDescription;
			return out;
		}
		
		/**
		 * Attempts to read CRLF.
		 * @return true if CRLF read, false if not (not CRLF, EOS reached).
		 * @throws IOException if a read error occurs.
		 */
		public boolean readCRLF() throws IOException
		{
			final int STATE_START = 0;
			final int STATE_CR = 1;
			int state = STATE_START;

			int r;
			char c;
			boolean keepGoing = true;
			while (keepGoing && (r = inputStream.read()) >= 0)
			{
				c = (char)r;
				switch (state)
				{
					case STATE_START:
					{
						if (c == '\r')
							state = STATE_CR;
						else
							return false;
					}
					break;
					
					case STATE_CR:
					{
						if (c == '\n')
							return true;
						else
							return false;
					}
				}
			}
			
			return false;
		}
		
		/**
		 * Attempts to read an HTTP header (plus CRLF).
		 * @return the header read, {@link Header#MALFORMED} if malformed, or <code>null</code> if a blank line (CRLF) was read.
		 * @throws IOException if a read error occurs.
		 */
		public Header readHeader() throws IOException
		{
			final int STATE_INIT = 0;
			final int STATE_NAME = 1;
			final int STATE_WHITESPACE = 2;
			final int STATE_VALUE = 3;
			final int STATE_CR = 4;
			int state = STATE_INIT;
			
			String name = null;
			String value = null;
			
			boolean keepGoing = true;
			StringBuilder sb = STRINGBUILDER.get();
			try 
			{
				int r;
				char c;
				while (keepGoing && (r = inputStream.read()) >= 0)
				{
					c = (char)r;
					switch (state)
					{
						case STATE_INIT:
						{
							if (c == '\r')
							{
								state = STATE_CR;
							}
							else if (Arrays.binarySearch(CHARS_HEADER_NAME, c) >= 0)
							{
								sb.append(c);
								state = STATE_NAME;
							}
							else
							{
								return Header.MALFORMED;
							}							
						}
						break;
						
						case STATE_NAME:
						{
							if (Arrays.binarySearch(CHARS_HEADER_NAME, c) >= 0)
							{
								sb.append(c);
							}
							else if (c == ':')
							{
								name = sb.toString();
								sb.delete(0, sb.length());
								state = STATE_WHITESPACE;
							}
							else
							{
								return Header.MALFORMED;
							}							
						}
						break;
						
						case STATE_WHITESPACE:
						{
							if (c == ' ')
							{
								// do nothing. eat character.
							}
							else if (c == '\r')
							{
								value = "";
								state = STATE_CR;
							}
							else
							{
								sb.append(c);
								state = STATE_VALUE;
							}
						}
						break;

						case STATE_VALUE:
						{
							if (c == '\r')
							{
								value = sb.toString();
								sb.delete(0, sb.length());
								state = STATE_CR;
							}
							else
							{
								sb.append(c);
							}
						}
						break;

						case STATE_CR:
						{
							if (c == '\n')
							{
								keepGoing = false;
							}
							else
							{
								return Header.MALFORMED;
							}							
						}
						break;
					}
				}
			} 
			finally 
			{
				sb.delete(0, sb.length());
			}

			// if true, EOS happened.
			if (keepGoing)
				return Header.MALFORMED;
			
			// read CRLF
			if (name == null)
				return null;
			
			Header out = new Header();
			out.name = name;
			out.value = value;
			return out;
		}

		/**
		 * Attempts to read a hex integer value (plus CRLF).
		 * @return the integer read, or null if bad integer parse.
		 * @throws IOException if a read error occurs.
		 */
		public Integer readHexInteger() throws IOException
		{
			final int STATE_START = 0;
			final int STATE_CR = 1;
			int state = STATE_START;

			int out = 0;
			
			int r;
			char c;
			while ((r = inputStream.read()) >= 0)
			{
				c = (char)r;
				switch (state)
				{
					case STATE_START:
					{
						int n;
						if ((n = HEXALPHABET.indexOf(c)) >= 0)
							out = (out << 4) + n;
						else if (c == '\r')
							state = STATE_CR;
						else
							return null;
					}
					break;
					
					case STATE_CR:
					{
						if (c == '\n')
							return out;
						else
							return null;
					}
				}
			}
			
			return null;
		}
		
		/**
		 * Reads bytes until a blank CRLF line is read.
		 * @param maxBytes the maximum amount of bytes to read
		 * @return true if reached before max bytes, false if not.
		 * @throws IOException if a read error occurs.
		 */
		public boolean skipSection(int maxBytes) throws IOException
		{
			final int STATE_START = 0;
			final int STATE_CR = 1;
			final int STATE_LF = 2;
			final int STATE_CR2 = 3;
			int state = STATE_START;

			int r;
			char c;
			while (maxBytes-- > 0 && (r = inputStream.read()) >= 0)
			{
				c = (char)r;
				switch (state)
				{
					case STATE_START:
					{
						if (c == '\r')
							state = STATE_CR;
						else
						{
							// do nothing. eat character.
						}
					}
					break;
					
					case STATE_CR:
					{
						if (c == '\n')
							state = STATE_LF;
						else
							state = STATE_START;
					}
					break;
					
					case STATE_LF:
					{
						if (c == '\r')
							state = STATE_CR2;
						else
							state = STATE_START;
					}
					break;

					case STATE_CR2:
					{
						if (c == '\n')
							return true;
						else
							state = STATE_START;
					}
					break;
				}
			}
			
			return false;
		}
		
		/**
		 * @return the underlying input stream.
		 */
		public InputStream getInputStream() 
		{
			return inputStream;
		}
		
	}
	
}
