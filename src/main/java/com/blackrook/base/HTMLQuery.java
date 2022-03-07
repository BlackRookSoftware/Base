/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple HTML scraping and querying class.
 * Leverages SAX to do the reading.
 * @author Matthew Tropiano
 */
public class HTMLQuery implements Iterable<HTMLQuery.Element>
{
	private static final HTMLQuery EMPTY_QUERY = new HTMLQuery();
	private static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);
	
	/**
	 * Parses a document from a string into this query structure.
	 * @param data the string data.
	 * @return a new query object for querying the document.
	 * @throws IOException if a read error occurred.
	 * @throws SAXException if a parse exception occurred.
	 */
	public static HTMLQuery readDocument(String data) throws IOException, SAXException
	{
		return readDocument(new StringReader(data));
	}

	/**
	 * Parses a document from a file into this query structure.
	 * Assumes native encoding.
	 * @param sourceFile the source file. 
	 * @return a new query object for querying the document.
	 * @throws IOException if a read error occurred.
	 * @throws SAXException if a parse exception occurred.
	 */
	public static HTMLQuery readDocument(File sourceFile) throws IOException, SAXException
	{
		try (Reader reader = new InputStreamReader(new FileInputStream(sourceFile)))
		{
			return readDocument(reader);
		}
	}

	/**
	 * Parses a document from a file into this query structure.
	 * Assumes native encoding.
	 * @param sourceFile the source file. 
	 * @param encoding the file encoding.
	 * @return a new query object for querying the document.
	 * @throws IOException if a read error occurred.
	 * @throws SAXException if a parse exception occurred.
	 */
	public static HTMLQuery readDocument(File sourceFile, Charset encoding) throws IOException, SAXException
	{
		try (Reader reader = new InputStreamReader(new FileInputStream(sourceFile), encoding))
		{
			return readDocument(reader);
		}
	}

	/**
	 * Parses a document from a string into this query structure.
	 * @param in the input stream.
	 * @param encoding the input stream encoding.
	 * @return a new query object for querying the document.
	 * @throws IOException if a read error occurred.
	 * @throws SAXException if a parse exception occurred.
	 */
	public static HTMLQuery readDocument(InputStream in, Charset encoding) throws IOException, SAXException
	{
		try (Reader reader = new InputStreamReader(in, encoding))
		{
			return readDocument(reader);
		}
	}

	/**
	 * Parses a document into this query structure.
	 * @param reader the reader to use.
	 * @return a new query object for querying the document.
	 * @throws IOException if a read error occurred.
	 * @throws SAXException if a parse exception occurred.
	 */
	public static HTMLQuery readDocument(Reader reader) throws IOException, SAXException
	{
		HTMLQuery out;
		SAXParser parser;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			parser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new SAXException("A parser exception occurred.", e);
		}
		InputSource source = new InputSource(reader);
		DocumentHandler handler = new DocumentHandler(out = new HTMLQuery());
		parser.parse(source, handler);
		return out;
	}

	// Wraps an element in a query.
	private static HTMLQuery wrap(Element element)
	{
		if (element == null)
			return EMPTY_QUERY;
		HTMLQuery query = new HTMLQuery();
		query.addElement(element);
		return query;
	}
	
	// Wraps a set of elements in a query.
	private static HTMLQuery wrap(List<Element> elements)
	{
		if (elements == null || elements.isEmpty())
			return EMPTY_QUERY;
		HTMLQuery query = new HTMLQuery();
		for (Element element : elements)
			query.addElement(element);
		return query;
	}
	
	/* ==================================================================== */
	
	/** List of elements. */
	private List<Element> elements;
	/** Map of id to element. */
	private Map<String, Element> idMap;
	/** Map of class to elements. */
	private Map<String, List<Element>> classMap;
	/** Map of attribute presence to elements. */
	private Map<String, List<Element>> attributeMap;
	/** Map of tag type to elements. */
	private Map<String, List<Element>> tagMap;
	
	private HTMLQuery(int capacity)
	{
		this.elements = new ArrayList<>(capacity);
		this.idMap = null;
		this.classMap = null;
		this.attributeMap = null;
		this.tagMap = null;
	}

	private HTMLQuery()
	{
		this(8);
	}

	private void addElement(Element element)
	{
		elements.add(element);
		String value;
		
		value = element.getId();
		if (value != null)
		{
			if (idMap == null)
				idMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			idMap.put(value, element);
		}
		
		for (String c : element.getClasses())
		{
			if (classMap == null)
				classMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			List<Element> list;
			if ((list = classMap.get(c)) == null)
				classMap.put(c, list = new LinkedList<>());
			list.add(element);
		}
		
		for (String a : element.getAttributes())
		{
			if (attributeMap == null)
				attributeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			List<Element> list;
			if ((list = attributeMap.get(a)) == null)
				attributeMap.put(a, list = new LinkedList<>());
			list.add(element);
		}
		
		value = element.getTag();
		if (value != null)
		{
			if (tagMap == null)
				tagMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			List<Element> list;
			if ((list = tagMap.get(value)) == null)
				tagMap.put(value, list = new LinkedList<>());
			list.add(element);
		}
	}
	
	/**
	 * Gets an element in this query by index.
	 * @param index the index.
	 * @return the corresponding element, or null if out of range.
	 */
	public Element getElement(int index)
	{
		if (index < 0 || index >= size())
			return null;
		return elements.get(index);
	}
	
	/**
	 * @return the amount of elements in this query.
	 */
	public int size() 
	{
		return elements.size();
	}
	
	/**
	 * @return true if this query is empty.
	 */
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	@Override
	public Iterator<Element> iterator() 
	{
		return elements.iterator();
	}
	
	/**
	 * Creates a new query containing a single element from this query.
	 * @param index the index of the element.
	 * @return a new query.
	 * @see #getElement(int)
	 */
	public HTMLQuery get(int index)
	{
		wrap(getElement(index));
		Element element = getElement(index);
		if (element == null)
			return EMPTY_QUERY;
		
		HTMLQuery query = new HTMLQuery();
		query.addElement(element);
		return query;
	}
	
	/**
	 * Creates a new query containing elements from this query that pass the provided filter function.
	 * If the function returns true for an element, it is added to this one.
	 * @param filterFunction the filter function.
	 * @return a new query.
	 */
	public HTMLQuery filter(Function<Element, Boolean> filterFunction)
	{
		final HTMLQuery query = new HTMLQuery();
		forEach((element) -> {
			if (filterFunction.apply(element))
				query.addElement(element);
		});
		return query;
	}
	
	/**
	 * Creates a new query by finding an element that matches an id.
	 * @param id the id to find.
	 * @return a new query.
	 */
	public HTMLQuery id(String id)
	{
		return wrap(idMap != null ? idMap.get(id) : null);
	}
	
	/**
	 * Creates a new query by finding elements that match a class name.
	 * @param className the class name to find.
	 * @return a new query.
	 */
	public HTMLQuery className(String className)
	{
		return wrap(classMap != null ? classMap.get(className) : null);
	}
	
	/**
	 * Creates a new query by finding elements that match an attribute name.
	 * @param attributeName the attribute name to find.
	 * @return a new query.
	 */
	public HTMLQuery attribute(String attributeName)
	{
		return wrap(attributeMap != null ? attributeMap.get(attributeName) : null);
	}
	
	/**
	 * Creates a new query by finding elements that match a tag name.
	 * @param tagName the tag name to find.
	 * @return a new query.
	 */
	public HTMLQuery tag(String tagName)
	{
		return wrap(tagMap != null ? tagMap.get(tagName) : null);
	}
	
	/* ==================================================================== */

	/**
	 * An Element.
	 */
	public static class Element implements Iterable<Element>
	{
		private static final AtomicLong IDENTIFIERGEN = new AtomicLong(0L);
		private static final List<Element> NO_CHILDREN = Collections.emptyList();
		private static final Map<String, String> NO_ATTRIBUTES = Collections.emptyMap();
		private static final Set<String> NO_CLASSES = Collections.emptySet();
		
		private static final String ATTRIBUTE_ID = "id";
		private static final String ATTRIBUTE_CLASS = "class";
		
		// Generated unique identifier.
		private Long identifier;
		
		private String tag;
		private Map<String, String> attributes;

		// === Lineage
		
		private Element parent;
		private List<Element> children;
		
		// === Special attributes to track
		
		private String id;
		private Set<String> classes;
		private String text;
		
		private Element()
		{
			this.identifier = IDENTIFIERGEN.getAndIncrement();
			this.tag = null;
			this.attributes = null;
			this.parent = null;
			this.children = null;
			this.id = null;
			this.classes = null;
			this.text = null;
		}

		/**
		 * @return true if this is a root element (no parent).
		 */
		public boolean isRoot()
		{
			return parent == null;
		}
		
		/**
		 * @return this element's parent, if any.
		 */
		public Element getParent() 
		{
			return parent;
		}
		
		/**
		 * @return the list of this element's children, if any.
		 */
		public List<Element> getChildren() 
		{
			return children != null ? children : NO_CHILDREN;
		}
		
		/**
		 * @return this element's id, if any.
		 */
		public String getId() 
		{
			return id;
		}
		
		/**
		 * @return the element tag name, if any.
		 */
		public String getTag() 
		{
			return tag;
		}

		/**
		 * @return a set of classes used by this element.
		 */
		public Set<String> getClasses()
		{
			return classes != null ? Collections.unmodifiableSet(classes) : NO_CLASSES;
		}
		
		/**
		 * @return a set of attribute names on this element.
		 */
		public Set<String> getAttributes()
		{
			return Collections.unmodifiableSet((attributes != null ? attributes : NO_ATTRIBUTES).keySet());
		}
		
		/**
		 * Gets an attribute's value on this element.
		 * @param attributeName the attribute name.
		 * @return the corresponding value, or <code>null</code> if not found.
		 */
		public String getAttribute(String attributeName)
		{
			return getAttribute(attributeName, null);
		}
		
		/**
		 * Gets an attribute's value on this element.
		 * @param attributeName the attribute name.
		 * @param defaultValue if not found, this value is returned.
		 * @return the corresponding value, or <code>defaultValue</code> if not found.
		 */
		public String getAttribute(String attributeName, String defaultValue)
		{
			return (attributes != null ? attributes : NO_ATTRIBUTES).getOrDefault(attributeName, defaultValue);
		}
		
		/**
		 * Checks if this element has a certain class present.
		 * @param className the class name.
		 * @return true if so, false if not.
		 */
		public boolean hasClass(String className)
		{
			return (classes != null ? classes : NO_CLASSES).contains(className);
		}
		
		/**
		 * Checks if this element has an attribute present.
		 * @param attributeName the attribute name.
		 * @return true if so, false if not.
		 */
		public boolean hasAttribute(String attributeName)
		{
			return getAttribute(attributeName) != null;
		}
		
		/**
		 * @return the text content, if any.
		 */
		public String getText() 
		{
			return text;
		}
		
		private void setParent(Element parent) 
		{
			this.parent = parent;
		}
		
		private void addChild(Element child)
		{
			if (children == null)
				children = new ArrayList<>();
			children.add(child);
		}
		
		private void finishChildren()
		{
			if (children == null)
				return;
			((ArrayList<Element>)children).trimToSize();
			children = Collections.unmodifiableList(children);
		}
		
		private void setTag(String tag)
		{
			this.tag = tag;
		}
		
		private void setAttribute(String attributeName, String value)
		{
			if (attributeName.equalsIgnoreCase(ATTRIBUTE_ID))
			{
				this.id = value;
			}
			else if (attributeName.equalsIgnoreCase(ATTRIBUTE_CLASS))
			{
				for (String c : value.split("\\s+"))
					addClass(c);
			}
			
			if (attributes == null)
				attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			attributes.put(attributeName, value);
		}
		
		private void addClass(String className)
		{
			if (classes == null)
				classes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			classes.add(className);
		}
		
		private void setText(String text) 
		{
			this.text = text;
		}
		
		@Override
		public Iterator<Element> iterator()
		{
			return children != null ? children.iterator() : Collections.emptyIterator();
		}
		
		@Override
		public int hashCode() 
		{
			return Long.hashCode(identifier);
		}
		
		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof Element)
				return ((Element)obj).identifier == identifier;
			return super.equals(obj);
		}
		
		/**
		 * @return this element's inner HTML (or text).
		 */
		public String innerHTML()
		{
			StringBuilder sb = new StringBuilder();
			// TODO: Finish.
			return sb.toString();
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			// TODO: Finish.
			return sb.toString();
		}
		
	}

	/**
	 * Markup parser.
	 */
	private static class DocumentHandler extends DefaultHandler
	{
		private HTMLQuery query;
		private Deque<Element> elementStack;
		
		private DocumentHandler(HTMLQuery query)
		{
			this.query = query;
			this.elementStack = null;
		}
		
		private Element push()
		{
			Element out;
			Element parent = peek();
			elementStack.add(out = new Element());
			if (parent != null)
			{
				parent.addChild(out);
				out.setParent(parent);
			}
			return out;
		}
		
		private Element peek()
		{
			return elementStack.peekLast();
		}
		
		private Element pop()
		{
			return elementStack.pollLast();
		}
		
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException 
		{
			return new InputSource(EMPTY_STREAM);
		}
		
		@Override
		public void startDocument() throws SAXException 
		{
			elementStack = new LinkedList<>();
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
		{
			Element element = push();
			String tag = qName != null ? qName : localName;
			element.setTag(tag);
			for (int i = 0; i < attributes.getLength(); i++)
				element.setAttribute(attributes.getLocalName(i), attributes.getValue(i));
			query.addElement(element);
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException 
		{
			Element text = new Element();
			text.setText(new String(ch, start, length));
			Element parent = peek();
			text.setParent(parent);
			parent.addChild(text);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException 
		{
			pop().finishChildren();
		}
		
		@Override
		public void endDocument() throws SAXException 
		{
			((ArrayList<?>)query.elements).trimToSize();
		}
		
		@Override
		public void error(SAXParseException e) throws SAXException 
		{
			//throw new SAXException("Parse error occurred.", e);
		}
		
		@Override
		public void fatalError(SAXParseException e) throws SAXException 
		{
			//throw new SAXException("Fatal parse error occurred.", e);
		}
		
	}
	
}
