/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple utility functions around Sets.
 * @author Matthew Tropiano
 */
public final class SetUtils
{
	/**
	 * Returns a new Set that is the union of the objects in two sets,
	 * i.e. a set with all objects from both sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S union(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		for (T val : set1)
			out.add(val);
		for (T val : set2)
			out.add(val);
		return (S)out;
	}

	/**
	 * Returns a new Set that is the intersection of the objects in two sets,
	 * i.e. the objects that are present in both sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S intersection(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		
		S bigset = set1.size() > set2.size() ? set1 : set2;
		S smallset = bigset == set1 ? set2 : set1;
		
		for (T val : smallset)
		{
			if (bigset.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Returns a new Set that is the difference of the objects in two sets,
	 * i.e. the objects in the first set minus the objects in the second.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S difference(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		for (T val : set1)
		{
			if (!set2.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Returns a new Set that is the union minus the intersection of the objects in two sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S xor(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		for (T val : set1)
		{
			if (!set2.contains(val))
				out.add(val);
		}
		for (T val : set2)
		{
			if (!set1.contains(val))
				out.add(val);
		}
		return (S)out;
	}

}
