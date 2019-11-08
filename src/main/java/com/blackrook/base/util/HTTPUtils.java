/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * HTTP Utilities.
 * @author Matthew Tropiano
 */
public final class HTTPUtils
{
	/** HTTP Method: GET. */
	public static final String HTTP_METHOD_GET = "GET"; 
	/** HTTP Method: HEAD. */
	public static final String HTTP_METHOD_HEAD = "HEAD";
	/** HTTP Method: DELETE. */
	public static final String HTTP_METHOD_DELETE = "DELETE"; 
	/** HTTP Method: OPTIONS. */
	public static final String HTTP_METHOD_OPTIONS = "OPTIONS"; 
	/** HTTP Method: TRACE. */
	public static final String HTTP_METHOD_TRACE = "TRACE"; 
	
	/** HTTP Method: POST. */
	public static final String HTTP_METHOD_POST = "POST";
	/** HTTP Method: PUT. */
	public static final String HTTP_METHOD_PUT = "PUT";
	/** HTTP Method: PATCH. */
	public static final String HTTP_METHOD_PATCH = "PATCH";

	private HTTPUtils() {}
	
	// Keep alphabetical.
	private static final String[] VALID_HTTP = new String[]{"http", "https"};
	
	/**
	 * Interface for reading an HTTPResponse from a URL call.
	 * @param <R> the return type.
	 */
	@FunctionalInterface
	public interface HTTPReader<R>
	{
		/**
		 * Called to read the HTTP response from an HTTP call.
		 * @param response the response object.
		 * @return the returned decoded object.
		 * @throws IOException if read error occurs.
		 */
		R onHTTPResponse(HTTPResponse response) throws IOException;
	}

	/**
	 * Response from an HTTP call.
	 */
	public static class HTTPResponse
	{
		private int statusCode;
		private String statusMessage;
		private String location;
		private URL referrer;
		private int length;
		private InputStream input;
		private String charset;
		private String contentType;
		private String contentTypeHeader;
		private String encoding;
		private boolean redirected;
	
		private HTTPResponse() {}
		
		/**
		 * @return the response status code.
		 */
		public int getStatusCode()
		{
			return statusCode;
		}
	
		/**
		 * @return the response status message.
		 */
		public String getStatusMessage()
		{
			return statusMessage;
		}
	
		/**
		 * @return the url of the next location, if this is a 3xx redirect response.
		 */
		public String getLocation()
		{
			return location;
		}
	
		/**
		 * @return the response's content length.
		 */
		public int getLength() 
		{
			return length;
		}
	
		/**
		 * @return an open input stream for reading the response's content.
		 */
		public InputStream getInputStream() 
		{
			return input;
		}
		
		/**
		 * @return the response's charset. can be null.
		 */
		public String getCharset()
		{
			return charset;
		}
	
		/**
		 * @return the response's content type. can be null.
		 */
		public String getContentType()
		{
			return contentType;
		}
	
		/**
		 * @return the response's content type (full unparsed header). can be null.
		 */
		public String getContentTypeHeader()
		{
			return contentTypeHeader;
		}
	
		/**
		 * @return the response's encoding. can be null.
		 */
		public String getEncoding()
		{
			return encoding;
		}
	
		/**
		 * @return the response's referrer URL. can be null.
		 */
		public URL getReferrer()
		{
			return referrer;
		}
	
		/**
		 * @return true if this response was the result of a redirect, false otherwise.
		 */
		public boolean isRedirected()
		{
			return redirected;
		}
	}

	/**
	 * An HTTP Reader that reads byte content and returns a decoded String.
	 * Gets the string contents of the response, decoded using the response's charset.
	 */
	public static HTTPReader<String> STRING_CONTENT_READER = (response) ->
	{
		String charset;
		if ((charset = response.getCharset()) == null)
			throw new UnsupportedEncodingException("No charset specified.");
		
		char[] c = new char[16384];
		StringBuilder sb = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(response.getInputStream(), charset);

		int buf = 0;
		while ((buf = reader.read(c)) >= 0) 
			sb.append(c, 0, buf);
		
		return sb.toString();
	};
	
	/**
	 * Multipart form data.
	 */
	public static class MultipartFormData
	{
		private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

		private static final byte[] BOUNDARY_START;
		private static final byte[] BOUNDARY_CRLF;
		private static final byte[] BOUNDARY_CONTINUE;
		private static final byte[] BOUNDARY_END;
		
		static
		{
			try {
				BOUNDARY_START = "--".getBytes("ASCII");
				BOUNDARY_CRLF = "\r\n".getBytes("ASCII");
				BOUNDARY_CONTINUE = "\r\n--".getBytes("ASCII");
				BOUNDARY_END = "--\r\n".getBytes("ASCII");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("JVM does not support ASCII encoding [INTERNAL ERROR].", e);
			}
		}
		
		/**
		 * Part data.
		 */
		private interface PartData
		{
			/**
			 * @return the disposition name of this part (usually a "file name") (can be null).
			 */
	        String getFileName();

	        /**
	         * @return the content type (MIME-type) of this part.
	         */
	        String getContentType();
	        
	        /**
	         * @return the content length of this part in bytes.
	         */
	        long getContentLength();
	        
	        /**
	         * @return the charset encoding of the (presumed string) data in this part (can be null). 
	         */
	        String getCharsetName();
	        
	        /**
	         * @return an open input stream to read from this part.
	         * @throws IOException if an I/O error occurs on read.
	         */
	        InputStream getInputStream() throws IOException;
		}
		
		/**
		 * A single form part.
		 */
		private static class Part
		{
			private String name;
			private PartData data;
			
			private Part(String name, PartData data)
			{
				this.name = name;
				this.data = data;
			}
		}
		
		/** The MIME-Type of the form data. */
		private String mimeType;
		/** The form part boundary. */
		private byte[] boundary;
		/** List of Parts. */
		private List<Part> parts;
		
		private MultipartFormData() {}
		
		/**
		 * Creates a new FormData object to send in a POST/PATCH/PUT request.
		 * @return a new form object.
		 */
		public static MultipartFormData create()
		{
			MultipartFormData out = new MultipartFormData();
			out.mimeType = "multipart/form-data";
			out.parts = new LinkedList<>();
			
			Random r = new Random();
			StringBuilder sb = new StringBuilder();
			int dashes = r.nextInt(15) + 10;
			int letters = r.nextInt(24) + 16;
			while (dashes-- > 0)
				sb.append('-');
			while (letters-- > 0)
				sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
			try {
				out.boundary = sb.toString().getBytes("ASCII");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("JVM does not support ASCII encoding [INTERNAL ERROR].", e);
			}
			return out;
		}

		/**
		 * Adds a single field to this multipart form.
		 * @param name the field name.
		 * @param value the value.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
		 */
	    public MultipartFormData addField(String name, String value) throws IOException
	    {
	    	return addTextPart(name, "text/plain", "utf-8", value);
	    }
	    
		/**
		 * Adds a single text part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the text part.
		 * @param data the text data.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
		 */
	    public MultipartFormData addTextPart(String name, final String mimeType, final String data) throws IOException
	    {
	    	return addTextPart(name, mimeType, "utf-8", data);
	    }
	    
	    /**
		 * Adds a single text part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the text part.
	     * @param charset the charset name for encoding the text.
		 * @param text the text data.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
	     */
	    public MultipartFormData addTextPart(String name, final String mimeType, final String charset, final String text) throws IOException
	    {
	    	return addDataPart(name, mimeType, charset, null, text.getBytes(charset));
	    }
	    
	    /**
	     * Adds a file part to this multipart form.
	     * The name of the file is passed along.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
	     * @param data the file data.
		 * @return itself, for chaining.
	     */
	    public MultipartFormData addFilePart(String name, String mimeType, final File data)
	    {
	    	return addFilePart(name, mimeType, data.getName(), data);
	    }
	    
	    /**
	     * Adds a file part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
	     * @param fileName the file name to send (overridden).
	     * @param data the file data.
		 * @return itself, for chaining.
	     */
	    public MultipartFormData addFilePart(String name, String mimeType, final String fileName, final File data)
	    {
	    	parts.add(new Part(name, new PartData() 
	    	{
				@Override
				public long getContentLength() 
				{
					return data.length();
				}

				@Override
				public String getFileName() 
				{
					return fileName;
				}

				@Override
				public String getContentType() 
				{
					return mimeType;
				}
				
				@Override
				public String getCharsetName() 
				{
					return null;
				}

				@Override
				public InputStream getInputStream() throws IOException
				{
					return new FileInputStream(data);
				}
			}));
	    	return this;
	    }
	    
	    public MultipartFormData addDataPart(String name, String mimeType, byte[] dataIn)
	    {
	    	// TODO: Finish this.
	    	return this;
	    }
	    
	    public MultipartFormData addDataPart(String name, String mimeType, String fileName, byte[] dataIn)
	    {
	    	// TODO: Finish this.
	    	return this;
	    }
	    
	    public MultipartFormData addDataPart(String name, String mimeType, String charset, final String fileName, final byte[] dataIn)
	    {
	    	parts.add(new Part(name, new PartData() 
	    	{
				@Override
				public long getContentLength() 
				{
					return dataIn.length;
				}

				@Override
				public String getFileName() 
				{
					return fileName;
				}

				@Override
				public String getContentType() 
				{
					return mimeType;
				}
				
				@Override
				public String getCharsetName() 
				{
					return charset;
				}

				@Override
				public InputStream getInputStream()
				{
					return new ByteArrayInputStream(dataIn);
				}
			}));
	    	return this;
	    }
	    
	}
	
	/**
	 * Sends a GET request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpGet(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpGet(new URL(url), null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpGet(URL url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpGet(url, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a GET request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpGet(String url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_GET, new URL(url), headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpGet(URL url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_GET, url, headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpHead(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpHead(new URL(url), null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpHead(URL url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpHead(url, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpHead(String url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_HEAD, new URL(url), headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpHead(URL url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_HEAD, url, headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpDelete(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpDelete(new URL(url), null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpDelete(URL url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpDelete(url, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpDelete(String url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_DELETE, new URL(url), headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpDelete(URL url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_DELETE, url, headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpOptions(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpOptions(new URL(url), null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpOptions(URL url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpOptions(url, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpOptions(String url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_OPTIONS, new URL(url), headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpOptions(URL url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_OPTIONS, url, headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpTrace(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpTrace(new URL(url), null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpTrace(URL url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpTrace(url, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpTrace(String url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_TRACE, new URL(url), headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpTrace(URL url, Map<String, String> headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_TRACE, url, headers, null, 0, null, null, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a PUT request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the string data to send.
	 * @param dataType the content MIME-Type (charset is appended to this).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPut(String url, Map<String, String> headers, String data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, headers, data.getBytes("UTF-8"), dataType + "; charset=utf-8", null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the string data to send.
	 * @param dataType the content MIME-Type (charset is appended to this).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPut(URL url, Map<String, String> headers, String data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, headers, data.getBytes("UTF-8"), dataType + "; charset=utf-8", null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPut(String url, Map<String, String> headers, byte[] data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, headers, data, dataType, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPut(URL url, Map<String, String> headers, byte[] data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, headers, data, dataType, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param dataEncoding the data encoding type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPut(String url, Map<String, String> headers, byte[] data, String dataType, String dataEncoding, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_PUT, new URL(url), headers, new ByteArrayInputStream(data), data.length, dataType, dataEncoding, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param dataEncoding the data encoding type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPut(URL url, Map<String, String> headers, byte[] data, String dataType, String dataEncoding, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_PUT, url, headers, new ByteArrayInputStream(data), data.length, dataType, dataEncoding, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PATCH request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the string data to send.
	 * @param dataType the content MIME-Type (charset is appended to this).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPatch(String url, Map<String, String> headers, String data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPatch(url, headers, data.getBytes("UTF-8"), dataType + "; charset=utf-8", null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PATCH request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the string data to send.
	 * @param dataType the content MIME-Type (charset is appended to this).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPatch(URL url, Map<String, String> headers, String data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPatch(url, headers, data.getBytes("UTF-8"), dataType + "; charset=utf-8", null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PATCH request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPatch(String url, Map<String, String> headers, byte[] data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPatch(url, headers, data, dataType, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PATCH request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPatch(URL url, Map<String, String> headers, byte[] data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPatch(url, headers, data, dataType, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PATCH request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param dataEncoding the data encoding type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPatch(String url, Map<String, String> headers, byte[] data, String dataType, String dataEncoding, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_PATCH, new URL(url), headers, new ByteArrayInputStream(data), data.length, dataType, dataEncoding, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PATCH request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param dataEncoding the data encoding type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPatch(URL url, Map<String, String> headers, byte[] data, String dataType, String dataEncoding, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_PATCH, url, headers, new ByteArrayInputStream(data), data.length, dataType, dataEncoding, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the string data to send.
	 * @param dataType the content MIME-Type (charset is appended to this).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPost(String url, Map<String, String> headers, String data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, data.getBytes("UTF-8"), dataType + "; charset=utf-8", null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a POST request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the string data to send.
	 * @param dataType the content MIME-Type (charset is appended to this).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPost(URL url, Map<String, String> headers, String data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, data.getBytes("UTF-8"), dataType + "; charset=utf-8", null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a POST request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPost(String url, Map<String, String> headers, byte[] data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, data, dataType, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a POST request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPost(URL url, Map<String, String> headers, byte[] data, String dataType, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, data, dataType, null, socketTimeoutMillis, reader);
	}
	
	/**
	 * Sends a POST request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param dataEncoding the data encoding type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPost(String url, Map<String, String> headers, byte[] data, String dataType, String dataEncoding, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_POST, new URL(url), headers, new ByteArrayInputStream(data), data.length, dataType, dataEncoding, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param data the byte data to send.
	 * @param dataType the content MIME-Type.
	 * @param dataEncoding the data encoding type.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpPost(URL url, Map<String, String> headers, byte[] data, String dataType, String dataEncoding, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return getHTTPContent(HTTP_METHOD_POST, url, headers, new ByteArrayInputStream(data), data.length, dataType, dataEncoding, null, socketTimeoutMillis, reader);
	}

	/**
	 * Gets the content from a opening an HTTP URL.
	 * The connection is closed afterward.
	 * @param <R> the return type.
	 * @param requestMethod the request method.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param dataContentOut if not null, read from this stream and send this data in the content.
	 * @param dataLength the amount of data to send in bytes.
	 * @param dataContentType if data is not null, this is the content type. If this is null, uses "application/octet-stream".
	 * @param dataContentEncoding if data is not null, this is the encoding for the written data. Can be null.
	 * @param defaultResponseCharset if the response charset is not specified, use this one.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the read content from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @throws ProtocolException if the requestMethod is incorrect.
	 */
	public static <R> R getHTTPContent(String requestMethod, URL url, Map<String, String> headers, InputStream dataContentOut, int dataLength, String dataContentType, String dataContentEncoding, String defaultResponseCharset, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		if (Arrays.binarySearch(VALID_HTTP, url.getProtocol()) < 0)
			throw new IOException("This is not an HTTP URL.");
	
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(socketTimeoutMillis);
		conn.setRequestMethod(requestMethod);
		
		if (headers != null) for (Map.Entry<String, String> entry : headers.entrySet())
			conn.setRequestProperty(entry.getKey(), entry.getValue());
	
		// set up POST data.
		if (dataContentOut != null)
		{
			conn.setRequestProperty("Content-Length", String.valueOf(dataLength));
			conn.setRequestProperty("Content-Type", dataContentType == null ? "application/octet-stream" : dataContentType);
			if (dataContentEncoding != null)
				conn.setRequestProperty("Content-Encoding", dataContentEncoding);
			conn.setDoOutput(true);
			try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream()))
			{
				relay(dataContentOut, dos, 8192);
			}
		}
	
		HTTPResponse response = new HTTPResponse();
		response.statusCode = conn.getResponseCode();
		response.statusMessage = conn.getResponseMessage();
		response.length = conn.getContentLength();
		response.encoding = conn.getContentEncoding();
		response.contentTypeHeader = conn.getContentType();

		int mimeEnd = response.contentTypeHeader.indexOf(';');
		
		response.contentType = response.contentTypeHeader.substring(0, mimeEnd >= 0 ? mimeEnd : response.contentTypeHeader.length()).trim();
		
		int charsetindex;
		if ((charsetindex = response.contentTypeHeader.toLowerCase().indexOf("charset=")) >= 0)
		{
			int endIndex = response.contentTypeHeader.indexOf(";", charsetindex);
			if (endIndex >= 0)
				response.charset = response.contentTypeHeader.substring(charsetindex + "charset=".length(), endIndex).trim();
			else
				response.charset = response.contentTypeHeader.substring(charsetindex + "charset=".length()).trim();
		}
		
		if (response.charset == null)
			response.charset = defaultResponseCharset;
		
		response.location = conn.getHeaderField("Location"); // if any.
		response.input = new BufferedInputStream(conn.getInputStream());
		
		R out = reader.onHTTPResponse(response);
		conn.disconnect();
		return out;
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
	private static int relay(InputStream in, OutputStream out, int bufferSize) throws IOException
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
	private static int relay(InputStream in, OutputStream out, int bufferSize, int maxLength) throws IOException
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
	
}
