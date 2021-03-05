/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.map;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * A special hashed queue map that maps keys to lists of objects instead of just
 * a single object. The {@link #enqueue(Object, Object)} method in this case
 * will append an object to the end of list instead of replacing the contents.
 * The method {@link #size()}, however will only give the amount of lists in the table,
 * not added objects. The method {@link #get(Object)} returns the queue associated with
 * the key.
 * @author Matthew Tropiano
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class HashedQueueMap<K, V> extends HashMap<K, Deque<V>>
{
	private static final long serialVersionUID = -832012439499014181L;

	/** Default capacity for a new array. */
	public static final int DEFAULT_CAPACITY = 8;
	/** Default rehash ratio. */
	public static final float DEFAULT_REHASH = 0.75f;
	
	/**
	 * Creates a new hashed queue map with capacity DEFAULT_CAPACITY, rehash ratio DEFAULT_REHASH.
	 */
	public HashedQueueMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new hashed queue map with capacity <i>cap</i> and rehash ratio DEFAULT_REHASH. 
	 * @param capacity the initial table capacity. Must be nonzero and non-negative.
	 * @throws IllegalArgumentException if capacity is negative.
	 */
	public HashedQueueMap(int capacity)
	{
		this(capacity, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new hashed queue map.
	 * @param capacity the capacity. cannot be negative.
	 * @param rehashRatio the ratio of capacity/tablesize. if this ratio is exceeded, 
	 * the table's capacity is expanded, and the table is rehashed.
	 * @throws IllegalArgumentException if capacity is negative or ratio is 0 or less.
	 */
	public HashedQueueMap(int capacity, float rehashRatio)
	{
		super(capacity, rehashRatio);
	}

	/**
	 * Enqueues a value in the queue designated by a key.
	 * Adds a new queue if it doesn't exist already.
	 * @param key the key,
	 * @param value the value to add.
	 */
	public void enqueue(K key, V value)
	{
		Deque<V> queue = get(key);
		if (queue == null)
		{
			queue = new LinkedList<V>();
			put(key, queue);
		}
		queue.add(value);
	}
	
	/**
	 * Dequeues a value in the queue designated by a key.
	 * If the last object corresponding to the key was dequeued, the key is removed. 
	 * If no list is associated with the key, this returns null.
	 * @param key the key.
	 * @return the value dequeued from the corresponding queue, or null if no values.
	 */
	public V dequeue(K key)
	{
		Deque<V> queue = get(key);
		V out = null;
		if (queue != null)
		{
			out = queue.pollFirst();
			if (queue.size() == 0)
				remove(key);
		}
		return out;
	}
	
	/**
	 * Removes a value from a queue designated by a key.
	 * If the last object corresponding to the key was removed, the key is removed. 
	 * @param key the key.
	 * @param value the value.
	 * @return true if it was removed, false otherwise.
	 */
	public boolean removeValue(K key, V value)
	{
		Deque<V> queue = get(key);
		boolean out = false;
		if (queue != null)
		{
			out = queue.remove(value);
			if (queue.size() == 0)
				remove(key);
		}
		return out;
	}
	
}