/*******************************************************************************
 * Copyright (c) 2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

/**
 * Some utility methods for Enums.
 * @author Matthew Tropiano
 */
public final class EnumUtils
{
	/**
	 * Returns the enum instance of a class given class and name, or null if not a valid name.
	 * If value is null, this returns null.
	 * @param <T> the Enum object type.
	 * @param value the value to search for.
	 * @param enumClass the Enum class to inspect.
	 * @return the enum value or null if the target does not exist.
	 */
	public static <T extends Enum<T>> T getEnumInstance(String value, Class<T> enumClass)
	{
		if (value == null)
			return null;
		
		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Turns a set of enums into a map of some kind of key to enum.
	 * @param <C> the key type; must be {@link Comparable}.
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class. 
	 * @param keyProviderFunc the function that fetches the corresponding id to use for the provided enum value. First parameter is the ordinal from {@link Enum#ordinal()}.
	 * @return a new map.
	 */
	public static <C extends Comparable<C>, E extends Enum<E>> SortedMap<C, E> createMap(Class<E> enumClass, BiFunction<Integer, E, C> keyProviderFunc)
	{
		return Collections.unmodifiableSortedMap(fillEnumMap(invokeValues(enumClass), new TreeMap<>(), keyProviderFunc));
	}

	/**
	 * Turns a set of enums into a map of integer id to enum.
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class. 
	 * @param idProviderFunc the function that fetches the corresponding id to use for the provided enum value. First parameter is the ordinal from {@link Enum#ordinal()}.
	 * @return a new map.
	 */
	public static <E extends Enum<E>> SortedMap<Integer, E> createIntegerMap(Class<E> enumClass, BiFunction<Integer, E, Integer> idProviderFunc)
	{
		return createMap(enumClass, idProviderFunc);
	}

	/**
	 * Turns a set of enums into a map of ordinal to enum value.
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class. 
	 * @return a new map.
	 */
	public static <E extends Enum<E>> SortedMap<Integer, E> createOrdinalMap(Class<E> enumClass)
	{
		return createOrdinalMap(enumClass, 0);
	}

	/**
	 * Turns a set of enums into a map of ordinal to enum value.
	 * <p>Can optionally have an offset to add to them (if offset is 1, each enum maps to <code>enum.ordinal() + 1</code>).
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class. 
	 * @param offset the offset to add to each ordinal.
	 * @return a new map.
	 */
	public static <E extends Enum<E>> SortedMap<Integer, E> createOrdinalMap(Class<E> enumClass, final int offset)
	{
		return createMap(enumClass, (ordinal, e) -> ordinal + offset);
	}

	/**
	 * Turns a set of enums into a map of String to enum value.
	 * The Strings used are the enum's {@link Enum#name()}.
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class.
	 * @return a new map.
	 */
	public static <E extends Enum<E>> SortedMap<String, E> createNameMap(Class<E> enumClass)
	{
		return createMap(enumClass, (ordinal, e) -> e.name());
	}

	/**
	 * Turns a set of enums into a map of case-insensitive-resolving Strings to enum value.
	 * The Strings used are the enum's {@link Enum#name()}.
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class.
	 * @return a new map.
	 */
	public static <E extends Enum<E>> SortedMap<String, E> createCaseInsensitiveNameMap(Class<E> enumClass)
	{
		return createCaseInsensitiveEnumMap(enumClass, (ordinal, e) -> e.name());
	}

	/**
	 * Turns a set of enums into a map of case-insensitive-resolving Strings to enum value.
	 * @param <E> an Enum type.
	 * @param enumClass the Enum class.
	 * @param nameProviderFunc the function that fetches the corresponding string to use for the provided enum value. First parameter is the ordinal from {@link Enum#ordinal()}.
	 * @return a new map.
	 */
	public static <E extends Enum<E>> SortedMap<String, E> createCaseInsensitiveEnumMap(Class<E> enumClass, BiFunction<Integer, E, String> nameProviderFunc)
	{
		return Collections.unmodifiableSortedMap(fillEnumMap(invokeValues(enumClass), new TreeMap<>(String.CASE_INSENSITIVE_ORDER), nameProviderFunc));
	}

	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> E[] invokeValues(Class<E> enumClass)
	{
		try {
			Method valuesMethod = enumClass.getMethod("values");
			if (valuesMethod == null)
				throw new IllegalArgumentException("INTERNAL ERROR: No such method 'values'."); 
			return (E[])valuesMethod.invoke(null);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("INTERNAL ERROR: No such static method 'values'."); 
		} catch (SecurityException e) {
			throw new RuntimeException("INTERNAL ERROR: Cannot invoke 'values' method on enum class: invocation disallowed."); 
		} catch (IllegalAccessException e) {
			throw new RuntimeException("INTERNAL ERROR: Cannot invoke 'values' method on enum class: illegal access."); 
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("INTERNAL ERROR: Cannot invoke 'values' method on enum class: requires different arguments."); 
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Cannot invoke 'values' method on enum class: found method not static."); 
		}
	}
	
	// Fills a map and returns the map reference.
	private static <E extends Enum<E>, K, M extends Map<K, E>> M fillEnumMap(E[] values, M targetMap, BiFunction<Integer, E, K> keyProvider)
	{
		for (int i = 0; i < values.length; i++)
			targetMap.put(keyProvider.apply(i, values[i]), values[i]);
		return targetMap;
	}
	
}
