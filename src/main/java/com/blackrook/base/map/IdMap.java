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
public class IdMap<T> 
{
	/** Reference map. */
	private Map<T, WeakReference<T>> refMap;
	
	/**
	 * Creates a new IdMap.
	 */
	public IdMap()
	{
		this.refMap = new HashMap<>();
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
