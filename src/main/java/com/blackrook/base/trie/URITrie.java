/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.trie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A trie that organizes mapping URI patterns to values.
 * @author Matthew Tropiano
 * @param <V> the value type at a resolved node.
 */
public class URITrie<V>
{
	/** Token content for "default" path. */
	public static final String DEFAULT_TOKEN = "*";

	/** Root node. */
	private Node<V> root; 

	/**
	 * Creates a new blank URI Trie.
	 */
	public URITrie()
	{
		this.root = Node.createRoot();
	}
	
	/**
	 * Adds a path to the trie.
	 * @param uri the request URI path.
	 * @param entryPoint the mapped entry point.
	 * @throws ParseException if a path parsing error occurs.
	 * @throws PatternSyntaxException if a regex expression is invalid in one of the paths.
	 */
	public void add(String uri, V entryPoint)
	{
		final int STATE_START = 0;
		final int STATE_PATH = 1;
		final int STATE_VARIABLE = 2;
		final int STATE_REGEX = 3;
		final int STATE_VARIABLE_END = 4;

		Node<V> endNode = root;
		String currentVariable = null;
		
		uri = trimSlashes(uri);
		
		if (!isEmpty(uri))
		{
			StringBuilder sb = new StringBuilder();
			int state = STATE_START;
			
			for (int i = 0; i < uri.length(); i++)
			{
				char c = uri.charAt(i);
				switch (state)
				{
					case STATE_START:
						if (c == '/')
						{
							// Do nothing
						}
						else if (c == '{')
							state = STATE_VARIABLE;
						else
						{
							sb.append(c);
							state = STATE_PATH;
						}
						break;
					
					case STATE_PATH:
						if (c == '/')
						{
							String token = sb.toString().trim();
							if (token.equals(DEFAULT_TOKEN))
								throw new ParseException("Wildcard token must be the last segment.");
							
							endNode.edges.add(endNode = Node.createMatchNode(token));
							sb.delete(0, sb.length());
						}
						else
						{
							sb.append(c);
						}
						break;
					
					case STATE_VARIABLE:
						if (c == ':')
						{
							currentVariable = sb.toString();
							sb.delete(0, sb.length());
							state = STATE_REGEX;
						}
						else if (c == '}')
						{
							endNode.edges.add(endNode = Node.createVariableNode(sb.toString().trim(), null));
							sb.delete(0, sb.length());
							state = STATE_VARIABLE_END;
						}
						else
						{
							sb.append(c);
						}
						break;
					
					case STATE_REGEX:
						if (c == '}')
						{
							endNode.edges.add(endNode = Node.createVariableNode(currentVariable, Pattern.compile(sb.toString().trim())));
							sb.delete(0, sb.length());
							state = STATE_VARIABLE_END;
						}
						else
						{
							sb.append(c);
						}
						break;
					
					case STATE_VARIABLE_END:
						if (c == '/')
							state = STATE_START;
						else
							throw new ParseException("Expected '/' to terminate path segment.");
						break;
						
				}// switch
			}// for
			
			if (state == STATE_VARIABLE)
				throw new ParseException("Expected '}' to terminate variable segment.");
			if (state == STATE_REGEX)
				throw new ParseException("Expected '}' to terminate variable regex segment.");
			
			if (sb.length() > 0)
			{
				String token = sb.toString().trim();
				if (token.equals(DEFAULT_TOKEN))
					endNode.edges.add(endNode = Node.createDefaultNode());
				else
					endNode.edges.add(endNode = Node.createMatchNode(token));
			}
				
			
		}
		
		endNode.entryPoint = entryPoint;
	}
	
	/**
	 * Attempts to resolve an endpoint for a given URI.
	 * @param uri the input URI.
	 * @return a result object detailing the search.
	 */
	public Result<V> resolve(String uri)
	{
		Node<V> next = root;
		Result<V> out = new Result<V>();
		
		uri = trimSlashes(uri);
		Queue<String> pathTokens = new LinkedList<>();
		
		for (String p : uri.split("\\/"))
			pathTokens.add(p);

		while (next != null && next.type != NodeType.DEFAULT && !pathTokens.isEmpty())
		{
			String pathPart = pathTokens.poll();
			TreeSet<Node<V>> edgeList = next.edges;
			next = null;
			for (Node<V> edge : edgeList)
			{
				if (edge.matches(pathPart))
				{
					if (edge.type == NodeType.PATHVARIABLE)
						out.addVariable(edge.token, pathPart);
					next = edge;
					break;
				}
			}
		}
		
		if (next != null)
			out.value = next.entryPoint;
		
		return out;
	}
	
	private enum NodeType
	{
		ROOT,
		MATCH,
		PATHVARIABLE,
		DEFAULT;
	}

	/**
	 * A single node.
	 */
	private static class Node<V> implements Comparable<Node<V>>
	{
		NodeType type;
		String token;
		Pattern pattern;
		V entryPoint;
		TreeSet<Node<V>> edges;
		
		private Node(NodeType type, String token, Pattern pattern)
		{
			this.type = type;
			this.token = token;
			this.pattern = pattern;
			this.entryPoint = null;
			this.edges = new TreeSet<>();
		}
		
		static <V> Node<V> createRoot()
		{
			return new Node<V>(NodeType.ROOT, null, null);
		}
		
		static <V> Node<V> createMatchNode(String token)
		{
			return new Node<V>(NodeType.MATCH, token, null);
		}
		
		static <V> Node<V> createVariableNode(String token, Pattern pattern)
		{
			return new Node<V>(NodeType.PATHVARIABLE, token, pattern);
		}

		static <V> Node<V> createDefaultNode()
		{
			return new Node<V>(NodeType.DEFAULT, null, null);
		}

		@Override
		public int compareTo(Node<V> n)
		{
			return type != n.type 
					? type.ordinal() - n.type.ordinal() 
					: !token.equals(n.token) 
						? token.compareTo(n.token) 
						: pattern != null 
							? -1 
							: 0
			;
		}
		
		/**
		 * Tests if this node matches a path part.
		 * @param pathPart the part of the path to test.
		 * @return
		 */
		private boolean matches(String pathPart)
		{
			if (type == NodeType.ROOT || type == NodeType.DEFAULT)
				return true;
			else if (type == NodeType.MATCH)
			{
				if (!isEmpty(token))
					return token.equals(pathPart);
				else if (!isEmpty(pattern))
					return pattern.matcher(pathPart).matches();
				else
					return false;
			}
			else if (type == NodeType.PATHVARIABLE)
			{
				if (!isEmpty(pattern))
					return pattern.matcher(pathPart).matches();
				else
					return true;
			}
			else
				return false;
		}
		
		@Override
		public String toString() 
		{
			return type.name() + " " + token + (pattern != null ? ":" + pattern.pattern() : "") + " " + entryPoint;
		}
		
	}
	
	/**
	 * Result class after a URITrie search.
	 * @param <V> the value that this holds.
	 */
	public static class Result<V>
	{
		private Map<String, String> pathVariables;
		private V value; 
		
		private Result()
		{
			this.pathVariables = null;
			this.value = null;
		}
		
		private void addVariable(String var, String value)
		{
			if (pathVariables == null)
				pathVariables = new HashMap<>();
			pathVariables.put(var, value);
		}
		
		/**
		 * Checks if this result has a found entry point in it.
		 * @return true if so, false if not.
		 */
		public boolean hasEndpoint()
		{
			return value != null;
		}
		
		/**
		 * Gets the found value, if any.
		 * @return an entry point to call, or null if no entry point.
		 */
		public V getValue() 
		{
			return value;
		}
		
		/**
		 * Gets the map of found path variables, if any.
		 * @return the map of variables, or null if no variables.
		 */
		public Map<String, String> getPathVariables() 
		{
			return pathVariables;
		}
		
	}

	/**
	 * An exception thrown when 
	 */
	public static class ParseException extends RuntimeException
	{
		private static final long serialVersionUID = -7627728030467051063L;
		
		private ParseException(String message)
		{
			super(message);
		}
	}
	
	private static boolean isEmpty(Object obj)
	{
		return obj == null || (obj instanceof String && ((String)obj).trim().isEmpty());
	}

	/**
	 * Trims slashes from the ends.
	 * @param str the input string.
	 * @return the input string with slashes removed from both ends.
	 * @see #removeBeginningSlash(String)
	 * @see #removeEndingSlash(String)
	 */
	private static String trimSlashes(String str)
	{
		return removeBeginningSlash(removeEndingSlash(str));
	}

	/**
	 * Removes the beginning slashes, if any, from a string.
	 * @param str the string.
	 * @return the resulting string without a beginning slash.
	 */
	private static String removeBeginningSlash(String str)
	{
		if (isEmpty(str))
			return str;
		
		int i = 0;
		while (i < str.length() && str.charAt(i) == '/')
			i++;
		return i > 0 ? str.substring(i) : str;
	}

	/**
	 * Removes the ending slashes, if any, from a string.
	 * @param str the string.
	 * @return the resulting string without an ending slash.
	 */
	private static String removeEndingSlash(String str)
	{
		if (isEmpty(str))
			return str;
		
		int i = str.length();
		while (i > 0 && str.charAt(i - 1) == '/')
			i--;
		return i > 0 ? str.substring(0, i) : str;
	}
	
}
