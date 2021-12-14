/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A special hashed queue map that maps keys to hashed sets of objects instead of just
 * a single object. The {@link #add(Object, Object)} method in this case
 * will add an object to the hash instead of replacing the contents.
 * The method {@link #size()}, however will only give the amount of hashes in the table,
 * not added objects. The method {@link #get(Object)} returns the hash associated with
 * the key.
 * @author Matthew Tropiano
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class HashedHashMap<K, V> extends HashMap<K, Set<V>>
{
	private static final long serialVersionUID = -764520121184150210L;
	
	/** Default capacity for a new array. */
	public static final int DEFAULT_CAPACITY = 8;
	/** Default rehash ratio. */
	public static final float DEFAULT_REHASH = 0.75f;
	
	/** Number of elements in the table. */
	protected int hashCapacity;
	/** Rehashing ratio for rehashing. */
	protected float hashRehashRatio;

	/**
	 * Creates a new hashed queue map with capacity DEFAULT_CAPACITY, rehash ratio DEFAULT_REHASH.
	 * The created hashes have capacity DEFAULT_CAPACITY, rehash ratio DEFAULT_REHASH.
	 */
	public HashedHashMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_REHASH, DEFAULT_CAPACITY, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new hashed hash map with capacity <i>cap</i> and rehash ratio DEFAULT_REHASH. 
	 * The created hashes have capacity DEFAULT_CAPACITY, rehash ratio DEFAULT_REHASH.
	 * @param capacity the initial table capacity. Must be nonzero and non-negative.
	 * @throws IllegalArgumentException if capacity is negative.
	 */
	public HashedHashMap(int capacity)
	{
		this(capacity, DEFAULT_REHASH, DEFAULT_CAPACITY, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new hashed hash map.
	 * The created hashes have capacity DEFAULT_CAPACITY, rehash ratio DEFAULT_REHASH.
	 * @param capacity the capacity. cannot be negative.
	 * @param rehashRatio the ratio of capacity/tablesize. if this ratio is exceeded, 
	 * the table's capacity is expanded, and the table is rehashed.
	 * @throws IllegalArgumentException if capacity is negative or ratio is 0 or less.
	 */
	public HashedHashMap(int capacity, float rehashRatio)
	{
		this(capacity, rehashRatio, DEFAULT_CAPACITY, DEFAULT_REHASH);
	}

	/**
	 * Creates a new hashed hash map.
	 * The created hashes have rehash ratio DEFAULT_REHASH.
	 * @param capacity the capacity. cannot be negative.
	 * @param rehashRatio the ratio of capacity/tablesize. if this ratio is exceeded, 
	 * the table's capacity is expanded, and the table is rehashed.
	 * @param hashCapacity initial capacity of the new hashes in this map.
	 * @throws IllegalArgumentException if capacity is negative or ratio is 0 or less.
	 */
	public HashedHashMap(int capacity, float rehashRatio, int hashCapacity)
	{
		this(capacity, rehashRatio, hashCapacity, DEFAULT_REHASH);
	}

	/**
	 * Creates a new hashed hash map, defining characteristics for the created hashes.
	 * @param capacity the capacity. cannot be negative.
	 * @param rehashRatio the ratio of capacity/tablesize. if this ratio is exceeded, 
	 * the table's capacity is expanded, and the table is rehashed.
	 * @param hashCapacity initial capacity of the new hashes in this map.
	 * @param hashRehashRatio the ratio of capacity/tablesize of the new hashes in this map. if this ratio is exceeded, 
	 * the table's capacity is expanded, and the table is rehashed.
	 * @throws IllegalArgumentException if capacity is negative or ratio is 0 or less.
	 */
	public HashedHashMap(int capacity, float rehashRatio, int hashCapacity, float hashRehashRatio)
	{
		super(capacity, rehashRatio);
		this.hashCapacity = hashCapacity;
		this.hashRehashRatio = hashRehashRatio;
	}

	/**
	 * Adds a value in the hash designated by a key.
	 * Adds a new hash if it doesn't exist already.
	 * @param key the key,
	 * @param value the value to add.
	 */
	public void add(K key, V value)
	{
		Set<V> hash = get(key);
		if (hash == null)
		{
			hash = new HashSet<V>(hashCapacity, hashRehashRatio);
			put(key, hash);
		}
		hash.add(value);
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
		Set<V> hash = get(key);
		boolean out = false;
		if (hash != null)
		{
			out = hash.remove(value);
			if (hash.size() == 0)
				remove(key);
		}
		return out;
	}

	/**
	 * Checks if a value exists for a corresponding key.
	 * @param key the key.
	 * @param value the value.
	 * @return true if it exists, false otherwise.
	 * @since 2.21.0
	 */
	public boolean containsValue(K key, V value)
	{
		Set<V> hash = get(key);
		boolean out = false;
		if (hash != null)
			return hash.contains(value);
		return out;
	}
	
}