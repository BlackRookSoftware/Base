package com.blackrook.base.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A contiguous array that auto-expands and acts like a ring deque.
 * Searching is {@code O(n)}, additions to and removals from the beginning or end of the list are {@code O(1)} ({@code O(n)} otherwise). 
 * Memory use is {@code O(n)}.
 * @author Matthew Tropiano
 * @param <T> the type that this list contains.
 */
public class RingList<T> implements Deque<T>
{
	/** Macro for doubling list capacity on expand. */
	public static final int CAPACITY_INCREASE_DOUBLE = -1; 

	/** The default list capacity. */
	public static final int DEFAULT_CAPACITY = 16;

	/** The capacity increase amount. */
	private int capacityIncrease;
	/** Array of elements. */
	private Object[] elements;
	/** Index of queue start. */
	private int startIndex;
	/** Index of queue end. */
	private int endIndex;
	/** Queue size. */
	private int size;
	
	/**
	 * Creates a new ring list with default capacity that doubles in size on expand.
	 */
	public RingList()
	{
		this(DEFAULT_CAPACITY, CAPACITY_INCREASE_DOUBLE);
	}

	/**
	 * Creates a new ring list that doubles in size on expand.
	 * @param capacity the initial capacity (1 or greater).
	 * @throws IllegalArgumentException if capacity is less than 1 or capacity increase is less than 1.
	 */
	public RingList(int capacity)
	{
		this(capacity, CAPACITY_INCREASE_DOUBLE);
	}

	/**
	 * Creates a new ring list.
	 * @param capacity the initial capacity (1 or greater).
	 * @param capacityIncrease (1 or greater, or {@link #CAPACITY_INCREASE_DOUBLE} for doubling).
	 * @throws IllegalArgumentException if capacity is less than 1 or capacity increase is less than 1.
	 */
	public RingList(int capacity, int capacityIncrease)
	{
		if (capacity < 1)
			throw new IllegalArgumentException("capacity cannot be less than 1");
		if (capacityIncrease < CAPACITY_INCREASE_DOUBLE || capacity == 0)
			throw new IllegalArgumentException("capacity increase cannot be negative or 0");
		
		this.elements = new Object[capacity];
		this.capacityIncrease = capacityIncrease;
		this.startIndex = 0;
		this.endIndex = 0;
		this.size = 0;
	}

	// Auto-caster.
	@SuppressWarnings("unchecked")
	private T get(int index)
	{
		return (T)elements[index];
	}
	
	// Increments this index such that an incrementation past the end of the array will wrap it to 0.
	// Returns the next "true" index.
	private int incrementIndex(int index)
	{
		int next = index + 1;
		return next >= elements.length ? 0 : next;
	}
	
	// Decrements this index such that an decrementation past the start of the array will wrap it to the end.
	// Returns the next "true" index.
	private int decrementIndex(int index)
	{
		int next = index - 1;
		return next < 0 ? elements.length - 1 : next;
	}
	
	/**
	 * @return this list's capacity.
	 */
	public int getCapacity()
	{
		return elements.length;
	}
	
	/**
	 * Attempts to expand the capacity of this list.
	 * If the new size is smaller than or equal to the capacity, nothing happens.
	 * @param newCapacity the new capacity.
	 */
	public void expandCapacity(int newCapacity)
	{
		if (newCapacity <= getCapacity())
			return;
		if (isEmpty())
		{
			elements = new Object[newCapacity];
		}
		else if (endIndex > startIndex)
		{
			Object[] newElements = new Object[newCapacity];
			System.arraycopy(elements, startIndex, newElements, 0, size);
			elements = newElements;
			startIndex = 0;
			endIndex = size;
		}
		else // list is wrapped.
		{
			Object[] newElements = new Object[newCapacity];
			int segment = startIndex - elements.length;
			System.arraycopy(elements, startIndex, newElements, 0, segment);
			System.arraycopy(elements, 0, newElements, segment, endIndex);
			elements = newElements;
			startIndex = 0;
			endIndex = size;
		}
	}
	
	@Override
	public Object[] toArray()
	{
		if (isEmpty())
		{
			return new Object[0];
		}
		else if (endIndex > startIndex)
		{
			Object[] out = new Object[size];
			System.arraycopy(elements, startIndex, out, 0, size);
			return out;
		}
		else // list is wrapped.
		{
			Object[] out = new Object[size];
			int segment = startIndex - elements.length;
			System.arraycopy(elements, startIndex, out, 0, segment);
			System.arraycopy(elements, 0, out, segment, endIndex);
			return out;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> T[] toArray(T[] a)
	{
		if (a.length < size())
			return (T[])toArray();
		
		if (isEmpty())
		{
			// Do nothing.
		}
		else if (endIndex > startIndex)
		{
			System.arraycopy(elements, startIndex, a, 0, size);
		}
		else // list is wrapped.
		{
			int segment = startIndex - elements.length;
			System.arraycopy(elements, startIndex, a, 0, segment);
			System.arraycopy(elements, 0, a, segment, endIndex);
		}

		// null sentinel in accordance with toArray policy.
		if (size() < a.length)
			a[size()] = null;
		
		return a;
	}

	// Searches for an object and returns its index is any (-1 if not).
	private int searchFor(Object o)
	{
		for (int i = startIndex; i != endIndex; i = incrementIndex(i))
			if (o.equals(elements[i]))
				return i;
		return -1;
	}
	
	@Override
	public boolean contains(Object o)
	{
		return searchFor(o) >= 0;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object obj : c)
			if (!contains(obj))
				return false;
		return true;
	}

	@Override
	public boolean add(T e)
	{
		addLast(e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		boolean altered = false;
		for (T obj : c)
			altered = add(obj) || altered;
		return altered;
	}

	@Override
	public T remove()
	{
		if (isEmpty())
			throw new NoSuchElementException(); 
		return pollFirst();
	}

	// Removes an object at a specific index in this list.
	private T removeAt(int index)
	{
		// TODO Finish this!
		return null;
	}

	@Override
	public boolean remove(Object o)
	{
		int index = searchFor(o);
		if (index < 0)
			return false;
		removeAt(index);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean altered = false;
		for (Object obj : c)
			altered = remove(obj) || altered;
		return altered;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear()
	{
		Arrays.fill(elements, null);
		startIndex = 0;
		endIndex = 0;
		size = 0;
	}

	@Override
	public void addFirst(T e)
	{
		offerFirst(e);
	}

	@Override
	public void addLast(T e)
	{
		offerLast(e);
	}

	@Override
	public T removeFirst()
	{
		if (isEmpty())
			throw new NoSuchElementException(); 
		return pollFirst();
	}

	@Override
	public T removeLast()
	{
		if (isEmpty())
			throw new NoSuchElementException(); 
		return pollLast();
	}

	@Override
	public T getFirst()
	{
		if (isEmpty())
			throw new NoSuchElementException(); 
		return peekFirst();
	}

	@Override
	public T getLast()
	{
		if (isEmpty())
			throw new NoSuchElementException(); 
		return peekLast();
	}

	@Override
	public boolean offer(T e)
	{
		return offerLast(e);
	}

	@Override
	public T poll()
	{
		return pollFirst();
	}

	@Override
	public T element()
	{
		return getFirst();
	}

	@Override
	public T peek()
	{
		return peekFirst();
	}

	@Override
	public void push(T e)
	{
		addFirst(e);
	}

	@Override
	public T pop()
	{
		return removeFirst();
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	/**
	 * @return true when the size of this list is same as its capacity.
	 */
	public boolean isFull()
	{
		return size == elements.length;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		boolean added = false;
		sb.append("[");
		for (T t : this)
		{
			if (added)
				sb.append(", ");
			sb.append(t);
			added = true;
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Iterator<T> iterator()
	{
		return new RingListIterator();
	}

	@Override
	public Iterator<T> descendingIterator()
	{
		return new DescendingRingListIterator();
	}

	@Override
	public boolean offerFirst(T e)
	{
		if (isFull())
			expandCapacity(capacityIncrease == CAPACITY_INCREASE_DOUBLE ? getCapacity() * 2 : getCapacity() + capacityIncrease);
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offerLast(T e)
	{
		if (isFull())
			expandCapacity(capacityIncrease == CAPACITY_INCREASE_DOUBLE ? getCapacity() * 2 : getCapacity() + capacityIncrease);

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T pollFirst()
	{
		if (isEmpty())
			return null;
			
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pollLast()
	{
		if (isEmpty())
			return null;

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T peekFirst()
	{
		if (isEmpty())
			return null;

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T peekLast()
	{
		if (isEmpty())
			return null;

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFirstOccurrence(Object o)
	{
		if (isEmpty())
			return false;

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeLastOccurrence(Object o)
	{
		if (isEmpty())
			return false;

		// TODO Auto-generated method stub
		return false;
	}

	private class RingListIterator implements Iterator<T>
	{
		private int current;
		private int end;
		private boolean removed;
		
		public RingListIterator()
		{
			this.current = -1;
			this.end = 0;
			this.removed = false;
		}
		
		@Override
		public boolean hasNext() 
		{
			return incrementIndex(current) < end;
		}

		@Override
		public T next() 
		{
			if (!hasNext())
				throw new NoSuchElementException();

			if (!removed)
				current = incrementIndex(current);
			T out = get(current);
			removed = false;
			return out;
		}
		
		@Override
		public void remove() 
		{
			removeAt(current);
		}
		
	}

	// TODO: UNTESTED
	private class DescendingRingListIterator implements Iterator<T>
	{
		private int current;
		private int end;
		private boolean removed;
		
		public DescendingRingListIterator()
		{
			this.current = endIndex;
			this.end = startIndex;
			this.removed = false;
		}
		
		@Override
		public boolean hasNext() 
		{
			return decrementIndex(current) >= end;
		}

		@Override
		public T next() 
		{
			if (!hasNext())
				throw new NoSuchElementException();

			if (!removed)
				current = incrementIndex(current);
			T out = get(current);
			removed = false;
			return out;
		}
		
		@Override
		public void remove() 
		{
			removeAt(current);
		}
		
	}

}
