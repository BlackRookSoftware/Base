/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This is a component manager and dependency injection system.
 * Classes that are managed by this system declare their components via {@link Component},
 * their factories via {@link ComponentFactory}, and their instantiation via {@link Singleton} or {@link NonSingleton}. 
 * @author Matthew Tropiano
 */
public final class ComponentManager
{
	/**
	 * This annotation denotes a class that is instantiated and managed by this manager.
	 * <p>This must have a {@link Singleton} or {@link NonSingleton} annotation with it. 
	 */
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Component {}

	/**
	 * This annotation denotes that this class is used for creating instances of objects.
	 * It is instantiated once as a singleton (and doesn't require the {@link Singleton} annotation).
	 */
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ComponentFactory {}

	/**
	 * This annotation denotes that this factory method is used for creating instances of objects.
	 * <p>This must be a member of a {@link ComponentFactory}, and must have a {@link Singleton} or {@link NonSingleton} annotation as well. It can be static. 
	 */
	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ComponentProvider {}

	/**
	 * This annotation denotes the constructor to use for constructing the object (and injecting components).
	 * <p>This must be a constructor of a {@link Component}. 
	 */
	@Target({ElementType.CONSTRUCTOR})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ComponentConstructor {}

	/**
	 * This annotation denotes that this component will have only one instance and share it among its dependencies.
	 * <p>This must be used with a {@link Component}-annotated class or a {@link ComponentProvider}-annotated method in a {@link ComponentFactory}. 
	 */
	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Singleton {}

	/**
	 * This annotation denotes that this component is constructed for each dependent class.
	 * <p>This must be used with a {@link Component}-annotated class or a {@link ComponentProvider}-annotated method in a {@link ComponentFactory}. 
	 */
	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface NonSingleton {}

	/**
	 * This annotation denotes that the annotated parameter will, on construction, will be passed
	 * the dependent class that requires this component. The parameter type must be {@link Class}.
	 * <p>This must be used with a parameter in a {@link ComponentConstructor}-annotated constructor or 
	 * a {@link ComponentProvider}-annotated method in a {@link ComponentFactory}. 
	 */
	@Target({ElementType.PARAMETER})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstructingClass {}

	/**
	 * This annotation denotes a sort order value for classes that share a type with this one.
	 * All created classes are grouped by parent types, and getting a list of them returns them in a sorted order.
	 * <p>This must be used with a {@link Component}-annotated class or a {@link ComponentProvider}-annotated method in a {@link ComponentFactory}.
	 * An ordering of <code>0</code> is used for classes without this annotation. 
	 */
	@Target({ElementType.PARAMETER})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Ordering
	{
	    /** @return the ordering value (default 0). */
	    int value() default 0;
	}

	/**
	 * An exception thrown if a component can't be instantiated.
	 */
	public static class ComponentException extends RuntimeException
	{
		private static final long serialVersionUID = 3300582667298232153L;

		private ComponentException(String message)
		{
			super(message);
		}

		private ComponentException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
	
	/**
	 * A provider encapsulation. 
	 */
	private static class Provider
	{
		private Class<?> type;
		
		private Constructor<?> constructor;
		private Class<?> factoryClass;
		private Method factoryMethod;
		
		private Class<?>[] parameterTypes;
		
		Provider(Class<?> type, Constructor<?> constructor)
		{
			this.type = type;
			this.constructor = constructor;
			this.factoryClass = null;
			this.factoryMethod = null;
			this.parameterTypes = constructor.getParameterTypes();
		}
		
		Provider(Class<?> type, Class<?> factoryClass, Method factoryMethod)
		{
			this.type = type;
			this.constructor = null;
			this.factoryClass = factoryClass;
			this.factoryMethod = factoryMethod;
			this.parameterTypes = factoryMethod.getParameterTypes();
		}

	}
	
	/**
	 * Concatenates a set of arrays together, such that the contents of each
	 * array are joined into one array. Null arrays are skipped.
	 * @param arrays the list of arrays.
	 * @return a new array with all objects in each provided array added 
	 * to the resultant one in the order in which they appear.
	 */
	private static String[] joinArrays(String[]...  arrays)
	{
		int totalLen = 0;
		for (String[] a : arrays)
			if (a != null)
				totalLen += a.length;
		
		String[] out = (String[])Array.newInstance(String.class, totalLen);
		
		int offs = 0;
		for (String[] a : arrays)
		{
			System.arraycopy(a, 0, out, offs, a.length);
			offs += a.length;
		}
		
		return out;
	}

	/**
	 * Returns the fully-qualified names of all classes beginning with
	 * a certain string. This uses {@link Thread#getContextClassLoader()} on the current thread to find them.
	 * None of the classes are "forName"-ed into PermGen space.
	 * <p>This scan can be expensive, as this searches the contents of the entire classpath.
	 * @param prefix the String to use for lookup. Can be null.
	 * @return the list of class names.
	 * @throws RuntimeException if a JAR file could not be read for some reason.
	 */
	private static String[] getClasses(String prefix)
	{
		return joinArrays(getClasses(prefix, Thread.currentThread().getContextClassLoader()), getClassesFromClasspath(prefix));
	}

	/**
	 * Returns the fully-qualified names of all classes beginning with
	 * a certain string. None of the classes are "forName"-ed into PermGen/Metaspace.
	 * <p>This scan can be expensive, as this searches the contents of the entire {@link ClassLoader}.
	 * @param prefix the String to use for lookup. Can be null.
	 * @param classLoader the ClassLoader to look into.
	 * @return the list of class names.
	 * @throws RuntimeException if a JAR file could not be read for some reason.
	 */
	private static String[] getClasses(String prefix, ClassLoader classLoader)
	{
		if (prefix == null)
			prefix = "";
		
		List<String> outList = new ArrayList<String>(128);
		
		while (classLoader != null)
		{
			if (classLoader instanceof URLClassLoader)
				scanURLClassLoader(prefix, (URLClassLoader)classLoader, outList);
			
			classLoader = classLoader.getParent();
		}
		
		String[] out = new String[outList.size()];
		outList.toArray(out);
		return out;
	}

	/**
	 * Returns the fully-qualified names of all classes beginning with
	 * a certain string. None of the classes are "forName"-ed into PermGen/Metaspace.
	 * <p>This scan can be expensive, as this searches the contents of the entire {@link ClassLoader}.
	 * @param prefix the String to use for lookup. Can be null.
	 * @return the list of class names.
	 * @throws RuntimeException if a JAR file could not be read for some reason.
	 */
	private static String[] getClassesFromClasspath(String prefix)
	{
		if (prefix == null)
			prefix = "";
		
		List<String> outList = new ArrayList<String>(128);
	
		String classpath = System.getProperty("java.class.path");
		String[] files = classpath.split("(\\"+File.pathSeparator+")");
		
		for (String fileName : files)
		{
			File f = new File(fileName);
			if (!f.exists())
				continue;
			
			if (f.isDirectory())
				scanDirectory(prefix, outList, fileName, f);
			else if (f.getName().toLowerCase().endsWith(".jar"))
				scanJARFile(prefix, outList, f);
			else if (f.getName().toLowerCase().endsWith(".jmod"))
				scanJMODFile(prefix, outList, f);
		}
		
		String[] out = new String[outList.size()];
		outList.toArray(out);
		return out;
	}

	/**
	 * Decodes a URL-encoded string.
	 * @param inString the input string.
	 * @return the unescaped string.
	 */
	private static String urlUnescape(String inString)
	{
		StringBuilder sb = new StringBuilder();
		char[] chars = new char[2];
		int x = 0;
		
		final int STATE_START = 0;
		final int STATE_DECODE = 1;
		int state = STATE_START;
		
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			
			switch (state)
			{
				case STATE_START:
					if (c == '%')
					{
						x = 0;
						state = STATE_DECODE;
					}
					else
						sb.append(c);
					break;
				case STATE_DECODE:
					chars[x++] = c;
					if (x == 2)
					{
						int v = 0;
						try {
							v = Integer.parseInt(new String(chars), 16);
							sb.append((char)(v & 0x0ff));
					} catch (NumberFormatException e) {
							sb.append('%').append(chars[0]).append(chars[1]);
						}
						state = STATE_START;
					}
					break;
			}
		}
		
		if (state == STATE_DECODE)
		{
			sb.append('%');
			for (int n = 0; n < x; n++)
				sb.append(chars[n]);
		}
		
		return sb.toString();
	}

	// Scans a URL classloader.
	private static void scanURLClassLoader(String prefix, URLClassLoader classLoader, List<String> outList)
	{
		for (URL url : classLoader.getURLs())
		{
			if (url.getProtocol().equals("file"))
			{
				String startingPath = urlUnescape(url.getPath().substring(1));
				File file = new File(startingPath);
				if (file.isDirectory())
					scanDirectory(prefix, outList, startingPath, file);
				else if (file.getName().endsWith(".jar"))
					scanJARFile(prefix, outList, file);
			}
		}
	}

	/**
	 * Explodes a list of files into a larger list of files,
	 * such that all of the files in the resultant list are not
	 * directories, by traversing directory paths.
	 *
	 * The returned list is not guaranteed to be in any order
	 * related to the input list, and may contain files that are
	 * in the input list if they are not directories.
	 *
	 * @param files	the list of files to expand.
	 * @return	a list of all files found in the subdirectory search.
	 * @throws	NullPointerException if files is null.
	 */
	private static File[] explodeFiles(File ... files)
	{
		Queue<File> fileQueue = new LinkedList<File>();
		List<File> fileList = new ArrayList<File>();
	
		for (File f : files)
			fileQueue.add(f);
	
		while (!fileQueue.isEmpty())
		{
			File dequeuedFile = fileQueue.poll();
			if (dequeuedFile.isDirectory())
			{
				for (File f : dequeuedFile.listFiles())
					fileQueue.add(f);
			}
			else
			{
				fileList.add(dequeuedFile);
			}
		}
	
		File[] out = new File[fileList.size()];
		fileList.toArray(out);
		return out;
	}
	
	// Scans a directory for classes.
	private static void scanDirectory(String prefix, List<String> outList, String startingPath, File file)
	{
		for (File f : explodeFiles(file))
		{
			String path = f.getPath();
			int classExtIndex = path.endsWith(".class") ? path.indexOf(".class") : -1;
			if (classExtIndex >= 0 && !path.contains("$") && !path.endsWith("package-info.class") && !path.endsWith("module-info.class"))
			{
				String className = path.substring(startingPath.length()+1, classExtIndex).replaceAll("[\\/\\\\]", ".");
				if (className.startsWith(prefix))
					outList.add(className);
			}
		}
	}

	// Scans a JAR file
	private static void scanJARFile(String prefix, List<String> outList, File file)
	{
		try (ZipFile jarFile = new ZipFile(file)) 
		{
			Enumeration<? extends ZipEntry> zipEntries = jarFile.entries();
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = zipEntries.nextElement();
				String path = ze.getName();
				int classExtIndex = path.indexOf(".class");
				if (classExtIndex >= 0 && !path.contains("$") && !path.endsWith("package-info.class") && !path.endsWith("module-info.class"))
				{
					String className = path.substring(0, classExtIndex).replaceAll("[\\/\\\\]", ".");
					if (className.startsWith(prefix))
						outList.add(className);
				}
			}
			
		} catch (ZipException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Scans a JMOD file
	private static void scanJMODFile(String prefix, List<String> outList, File file)
	{
		try (ZipFile jmodFile = new ZipFile(file)) 
		{
			Enumeration<? extends ZipEntry> zipEntries = jmodFile.entries();
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = zipEntries.nextElement();
				String path = ze.getName();
				int classExtIndex = path.indexOf(".class");
				if (classExtIndex >= 0 && !path.contains("$") && !path.endsWith("package-info.class") && !path.endsWith("module-info.class"))
				{
					String className = path.substring(0, classExtIndex).replaceAll("[\\/\\\\]", ".");
					if (className.startsWith(prefix))
						outList.add(className);
				}
			}
			
		} catch (ZipException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#getDeclaredConstructor(Class...)} with no arguments 
	 * and {@link Class#newInstance()}, but wraps the call in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param clazz the class type to instantiate.
	 * @return a new instance of an object.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	private static <T> T create(Class<T> clazz)
	{
		Object out = null;
		try {
			out = clazz.getDeclaredConstructor().newInstance();
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		
		return clazz.cast(out);
	}

	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#newInstance()}, but wraps the call
	 * in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param constructor the constructor to call.
	 * @param params the constructor parameters.
	 * @return a new instance of an object created via the provided constructor.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T construct(Constructor<T> constructor, Object ... params)
	{
		Object out = null;
		try {
			out = (T)constructor.newInstance(params);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		return (T)out;
	}

	/*===============================================================*/
	
	/** List of root packages in the order to scan. */
	private String[] packageRoots;

	/** Map of class to Singleton instance. */
	private Map<Class<?>, Provider> providerMap;

	/** Set of singleton types. */
	private Set<Class<?>> singletonTypes;
	/** Set of non-singleton types. */
	private Set<Class<?>> nonSingletonTypes;

	/** Map of class to Singleton instance. */
	private Map<Class<?>, Object> singletonMap;
	/** Map of class to list of similar-typed singleton components. */
	private Map<Class<?>, List<Object>> singletonTypeMap;

	/**
	 * Creates a new component manager from a set of class package names.
	 * @param packages the list of packages.
	 * @return a new ComponentManager with all of the classes/components instantiated.
	 * @throws ComponentException if a component instantiation or setup encounters a problem.
	 */
	public static ComponentManager create(String... packages)
	{
		ComponentManager out = new ComponentManager(packages);
		out.scanPackages();
		// TODO out.createComponents();
		return out;
	}
	
	// Creates the component manager.
	private ComponentManager(String[] packageRoots)
	{
		this.packageRoots = packageRoots;
		this.providerMap = new HashMap<>();
		this.singletonTypes = new HashSet<>();
		this.nonSingletonTypes = new HashSet<>();
		this.singletonMap = new HashMap<>();
		this.singletonTypeMap = new HashMap<>();
	}
	
	// Scans all classes.
	private void scanPackages()
	{
		for (String packageName : packageRoots)
			for (String className : getClasses(packageName))
				scanClass(className);
	}
	
	// Scans all classes.
	private void scanClass(String className)
	{
		try {
			Class<?> clz = Class.forName(className);

			boolean component = clz.isAnnotationPresent(Component.class);
			boolean factory = clz.isAnnotationPresent(ComponentFactory.class);

			if (component)
			{
				if (factory)
					throw new ComponentException("Class \"" + clz.getName() + "\" cannot be both a @Component and @ComponentFactory. Must be one or the other.");
				scanComponent(clz);
			}
			else if (factory)
			{
				scanComponentFactory(clz);
			}
			else
				return;
			
		} catch (ClassNotFoundException e) {
			throw new ComponentException("Cannot find class " + className + " in classpath.", e);
		}
	}

	// Scans a single component.
	private void scanComponent(Class<?> clz)
	{
		if (providerMap.containsKey(clz) && singletonTypes.contains(clz))
		{
			Provider p = providerMap.get(clz);
			if (p.constructor != null)
				throw new ComponentException("Singleton Component \"" + clz.getName() + "\" is already being provided by... itself?? [INTERNAL ERROR]");
			else
				throw new ComponentException("Singleton Component \"" + clz.getName() + "\" is already being provided by component factory method " + p.factoryClass.getName() + "." + p.factoryMethod.getName());
		}

		boolean single = clz.isAnnotationPresent(Singleton.class);
		boolean nonsingle = clz.isAnnotationPresent(NonSingleton.class);
		
		if (single ^ nonsingle)
			throw new ComponentException("Component \"" + clz.getName() + "\" not specified as @Singleton or @NonSingleton. Must be one or the other, and not both.");

		Constructor<?> found = null;
		for (Constructor<?> cons : clz.getConstructors())
		{
			if (cons.isAnnotationPresent(ComponentConstructor.class))
			{
				if (found != null)
					throw new ComponentException("Component \"" + clz.getName() + "\" already had a constructor with a @ComponentConstructor annotation.");
				
				found = cons;
				if ((cons.getModifiers() & Modifier.PUBLIC) == 0)
					throw new ComponentException("Component \"" + clz.getName() + "\" cannot have a non-public constructor with a @ComponentConstructor annotation.");
			}
		}
		
		if (found == null)
		{
			try{
				found = clz.getDeclaredConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ComponentException("Component \"" + clz.getName() + "\" does not a have an implicit, accessible public constructor!");
			}
		}

		providerMap.put(clz, new Provider(clz, found));
		
		if (single)
			singletonTypes.add(clz);
		else // non-single
			nonSingletonTypes.add(clz);
	}
	
	// Scans a single component factory.
	private void scanComponentFactory(Class<?> clz)
	{
		if (providerMap.containsKey(clz))
			throw new ComponentException("Component Factory \"" + clz.getName() + "\" cannot be provided by another source, only itself.");

		Constructor<?> found = null;
		for (Constructor<?> cons : clz.getConstructors())
		{
			if (cons.isAnnotationPresent(ComponentConstructor.class))
			{
				if (found != null)
					throw new ComponentException("Component Factory \"" + clz.getName() + "\" already had a constructor with a @ComponentConstructor annotation.");
				
				found = cons;
				if ((cons.getModifiers() & Modifier.PUBLIC) == 0)
					throw new ComponentException("Component Factory \"" + clz.getName() + "\" cannot have a non-public constructor with a @ComponentConstructor annotation.");
			}
		}
		
		if (found == null)
		{
			try{
				found = clz.getDeclaredConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ComponentException("Component Factory \"" + clz.getName() + "\" does not a have an implicit, accessible public constructor!");
			}
		}
		
		providerMap.put(clz, new Provider(clz, found));
		singletonTypes.add(clz);
		
		for (Method method : clz.getDeclaredMethods())
		{
			if (method.isAnnotationPresent(ComponentProvider.class))
			{
				int modifiers = method.getModifiers();
				if ((modifiers & Modifier.PUBLIC) == 0)
					throw new ComponentException("Component Factory \"" + clz.getName() + "\" cannot have a non-public factory method with a @ComponentProvider annotation.");

				Class<?> type = method.getReturnType();
				
				if (type == Void.TYPE || type == Void.class)
					throw new ComponentException("Component Factory \"" + clz.getName() + "\" cannot have a factory method with a @ComponentProvider annotation that returns void: " + method.getName());
				if (type.isAnnotationPresent(Component.class))
					throw new ComponentException("Component Factory method \"" + clz.getName() + "." + method.getName() + "\" provides a @Component that provides itself!");
				if (type.isAnnotationPresent(ComponentFactory.class))
					throw new ComponentException("Component Factory method \"" + clz.getName() + "." + method.getName() + "\" provides a @ComponentFactory, which is illegal.");

				// static methods do not require an instance
				if ((modifiers & Modifier.STATIC) != 0)
					providerMap.put(type, new Provider(type, null, method));
				else
					providerMap.put(type, new Provider(type, clz, method));
				
				boolean single = method.isAnnotationPresent(Singleton.class);
				boolean nonsingle = method.isAnnotationPresent(NonSingleton.class);

				if (single ^ nonsingle)
					throw new ComponentException("Component Factory method \"" + clz.getName() + "." + method.getName() + "\" not specified as @Singleton or @NonSingleton. Must be one or the other, and not both.");

				if (single)
					singletonTypes.add(type);
				else // non-single
					nonSingletonTypes.add(type);
			}
		}
		
	}
	
	
}
