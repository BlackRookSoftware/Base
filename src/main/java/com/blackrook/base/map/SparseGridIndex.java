/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a grid that contains a grid of Object data generally used for maps and lookups.
 * This map is <i>sparse</i>, which means it uses as little memory as possible, which can increase the lookup time in most cases.
 * @author Matthew Tropiano
 * @param <T> the value type.
 */
public class SparseGridIndex<T> implements Iterable<Map.Entry<SparseGridIndex.Pair, T>>
{
	private static final ThreadLocal<Pair> CACHE_PAIR = ThreadLocal.withInitial(()->new Pair());
	
	/** List of grid codes. */
	protected Map<Pair, T> data;
	
	/**
	 * Creates a new sparse grid of an unspecified width and height.
	 * @throws IllegalArgumentException if capacity is negative or ratio is 0 or less.
	 */
	public SparseGridIndex()
	{
		data = new HashMap<Pair, T>();
	}
	
	/**
	 * Clears everything from the grid.
	 */
	public void clear()
	{
		data.clear();
	}

	/**
	 * Sets an object at a particular part of the grid.
	 * @param x	the grid position x to set info.
	 * @param y	the grid position y to set info.
	 * @param object the object to set. Can be null.
	 */
	public void set(int x, int y, T object)
	{
		Pair cp = CACHE_PAIR.get();
		cp.set(x, y);
		if (object == null)
			data.remove(cp);
		else
			data.put(new Pair(x, y), object);
	}

	/**
	 * Gets the object at a particular part of the grid.
	 * @param x	the grid position x to get info.
	 * @param y	the grid position y to get info.
	 * @return the object at that set of coordinates or null if not object.
	 */
	public T get(int x, int y)
	{
		Pair cp = CACHE_PAIR.get();
		cp.set(x, y);
		return data.get(cp);
	}
	
	@Override
	public String toString()
	{
		return data.toString();
	}

	@Override
	public Iterator<Map.Entry<Pair, T>> iterator()
	{
		return data.entrySet().iterator();
	}

	/**
	 * @return amount of elements in this collection.
	 */
	public int size()
	{
		return data.size();
	}

	/**
	 * @return true if this contains no elements, false otherwise.
	 */
	public boolean isEmpty()
	{
		return size() == 0;
	}

	/**
	 * Ordered Pair integer object. 
	 */
	public static class Pair
	{
		/** X-coordinate. */
		public int x;
		/** Y-coordinate. */
		public int y;
		
		/**
		 * Creates a new Pair (0,0).
		 */
		public Pair()
		{
			this(0, 0);
		}
		
		/**
		 * Creates a new Pair.
		 * @param x the x-coordinate value.
		 * @param y the y-coordinate value.
		 */
		public Pair(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode()
		{
			return x ^ ~y;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof Pair)
				return equals((Pair)obj);
			else
				return super.equals(obj);
		}
		
		/**
		 * Sets both components.
		 * @param x the x-coordinate value.
		 * @param y the y-coordinate value.
		 */
		public void set(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		/**
		 * Sets both components using an existing pair.
		 * @param p the source pair.
		 */
		public void set(Pair p)
		{
			this.x = p.x;
			this.y = p.y;
		}
		
		/**
		 * Checks if this pair equals another.
		 * @param p the other pair.
		 * @return true if so, false if not.
		 */
		public boolean equals(Pair p)
		{
			return x == p.x && y == p.y;
		}

		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ")";
		}
		
	}

}
