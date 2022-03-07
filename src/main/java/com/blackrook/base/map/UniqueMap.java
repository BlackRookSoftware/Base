/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.map;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A map that is used to produce unique instances of equality-having objects.
 * <p> The direct need for this is being able to use mutual exclusion 
 * (<code>synchronized</code>) on objects that would have strict equality
 * but tend to have a lack of uniqueness in reference, like Strings or Integers
 * or other common classes or boxed primitives. Since synchronization happens
 * on references and not "values," a structure such as this may need to be employed 
 * in order to use mutual exclusion or wait/notify on equal values tied to a common reference.
 * @author Matthew Tropiano
 * @param <T> the type that this map holds.
 */
public class UniqueMap<T> 
{
	private static final int DEFAULT_CAPACITY = 16;
	private static final float DEFAULT_REHASH = 0.75f;
	
	/** Reference map. */
	private Map<T, WeakReference<T>> refMap;
	
	/**
	 * Creates a new UniqueMap.
	 */
	public UniqueMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new UniqueMap with an initial capacity.
	 * @param capacity the map's initial capacity.
	 */
	public UniqueMap(int capacity)
	{
		this(capacity, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new UniqueMap with an initial capacity and rehash ratio.
	 * @param capacity the map's initial capacity.
	 * @param rehash the map's rehashing ratio.
	 */
	public UniqueMap(int capacity, float rehash)
	{
		this.refMap = new HashMap<>(capacity, rehash);
	}
	
	/**
	 * Gets the unique reference for a specific value instance.
	 * This method is thread-safe.
	 * @param value the value to use.
	 * @return a common reference for the provided value.
	 * @see #equals(Object)
	 * @see #hashCode()
	 */
	public T uniqueRef(T value)
	{
		WeakReference<T> out;
		synchronized (refMap)
		{
			if ((out = refMap.get(value)) == null)
				refMap.put(value, out = new WeakReference<T>(value));
		}
		return out.get();
	}
	
	/**
	 * Removes the reference for a value.
	 * If the entry does not exist, this does nothing.
	 * This method is thread-safe.
	 * @param value the value to use.
	 */
	public void releaseRef(T value)
	{
		if (!refMap.containsKey(value))
			return;
		
		synchronized (refMap) 
		{
			refMap.remove(value);
		}
	}
	
}
