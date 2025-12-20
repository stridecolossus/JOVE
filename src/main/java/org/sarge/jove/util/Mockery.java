package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.*;
import java.util.*;

/**
 * A <i>mockery</i> is a proxy implementation of an API.
 * <p>
 * The {@link #proxy()} implementation generates a {@link Mock} for each public, non-static member of the API.
 * Mocked methods can be retrieved and configured using {@link #mock(String)}.
 * <p>
 * Additionally mocked methods can be overridden with a concrete implementation via the {@link #implement(Object)} method.
 * <p>
 * @author Sarge
 */
public class Mockery {
	private final Map<Method, Mock> mocks = new HashMap<>();
	private final Object proxy;
	private final List<Class<?>> interfaces;

	/**
	 * Constructor.
	 * @param interfaces Interfaces to mock
	 */
	public Mockery(Class<?>... interfaces) {
		this.interfaces = List.of(interfaces);
		this.proxy = proxy(interfaces);
	}

	/**
	 * Convenience constructor that also implements the given concrete instance.
	 * @param instance			Instance to implement
	 * @param interfaces		Interfaces to mock
	 */
	public Mockery(Object instance, Class<?>... interfaces) {
		this(interfaces);
		implement(instance);
	}

	/**
	 * @return Proxy implementation
	 */
	@SuppressWarnings("unchecked")
	public <T> T proxy() {
		return (T) proxy;
	}

	/**
	 * Builds the proxy implementation.
	 */
	@SafeVarargs
	private Object proxy(Class<?>... interfaces) {
		final var handler = new InvocationHandler() {
    		@Override
    		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    			final Mock mock = mocks.computeIfAbsent(method, Mock::new);
    			return mock.invoke(args);
    		}
    	};

		final ClassLoader loader = this.getClass().getClassLoader();
		return Proxy.newProxyInstance(loader, interfaces, handler);
	}

	/**
	 * Mocks the given method.
	 * @param name Method name
	 * @return Mocked method
	 * @throws IllegalArgumentException if the method does not exist
	 * @throws IllegalStateException if the method name is ambiguous (i.e. overloaded)
	 * @throws IllegalStateException if the method is a concrete implementation
	 */
	public Mock mock(String name) {
		// Check for existing proxy
		final var prev = mocks
				.keySet()
				.stream()
				.filter(method -> method.getName().equals(name))
				.findAny()
				.map(mocks::get);

		if(prev.isPresent()) {
			return prev.get();
		}

		// Find method in API
		final List<Method> list = interfaces
				.stream()
				.map(Class::getDeclaredMethods)
				.flatMap(Arrays::stream)
				.filter(method -> method.getName().equals(name))
				.toList();

		// Check method is unique
		final Method method = switch(list.size()) {
			case 1 -> list.getFirst();
			case 0 -> throw new IllegalArgumentException("Unknown method: " + name);
			default -> throw new IllegalArgumentException("Ambiguous method: " + name);
		};

		// Register proxy method
		final var mock = new Mock(method);
		assert !mocks.containsKey(method);
		mocks.put(method, mock);

		return mock;
	}
	// TODO - find exact match by parameters

	/**
	 * Overrides proxy methods with the given concrete implementation.
	 * @param instance Concrete instance
	 * @throws IllegalArgumentException if any public method of {@link #instance} is not present in the API
	 */
	public void implement(Object instance) {
		for(Method method : instance.getClass().getDeclaredMethods()) {
			// Check whether appropriate method
			if(!isMockableMethod(method)) {
				continue;
			}

			// Lookup proxy method
			final Method actual = find(method);
			final var mock = mocks.computeIfAbsent(actual, Mock::new);

			// Delegate to concrete implementation
			mock.concrete = new ConcreteMethod(instance, method);
			mock.result = null;

			// Ensure method can be invoked
			method.setAccessible(true);
		}
	}

	/**
	 * A <i>concrete method</i> delegates invocations to a concrete implementation matching an API method.
	 */
	private record ConcreteMethod(Object instance, Method method) {
		/**
		 * Invokes this concrete implementation.
		 */
		private Object invoke(Object[] arguments) throws Throwable {
			try {
				return method.invoke(instance, arguments);
			}
			catch(InvocationTargetException e) {
				throw e.getTargetException();
			}
			catch(Exception e) {
				throw new RuntimeException("Error invoking concrete method: " + this, e);
			}
		}
	}

	/**
	 * @return Whether the given method can be mocked
	 */
	private static boolean isMockableMethod(Method method) {
		final int modifiers = method.getModifiers();
		return
				Modifier.isPublic(modifiers) &&
				!Modifier.isStatic(modifiers) &&
				!Modifier.isFinal(modifiers) &&
				!Modifier.isNative(modifiers);
	}

	/**
	 * Finds an API method matching the given concrete implementation.
	 */
	private Method find(Method method) {
		return interfaces
				.stream()
				.map(Class::getDeclaredMethods)
				.flatMap(Arrays::stream)
				.filter(m -> Mockery.matches(m, method))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Unknown API method: " + method));
	}

	/**
	 * @return Whether the given methods are equivalent
	 */
	private static boolean matches(Method left, Method right) {
		return
				left.getName().equals(right.getName()) &&
				(left.getReturnType() == right.getReturnType()) &&
				Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
	}

	/**
	 * A <i>mock</i> is a proxy implementation of an API method.
	 * <p>
	 * This class records the {@link #count()} of the number of invocations and returns the {@link #arguments()} of the most-recent invocation.
	 * <p>
	 * The return value is initialised to a suitable default value but can be overridden using {@link #result(Object)}.
	 * A mock can also be configured to {@link #fail(Throwable)} with a given exception.
	 * <p>
	 * Mocked methods can be overridden with a concrete implementation using {@link Mockery#implement(Object)}.
	 * <p>
	 */
	public static class Mock {
		private final Method method;
		private int count;
		private Object result;
		private Throwable exception;
		private Object[] arguments;
		private ConcreteMethod concrete;

		private Mock(Method method) {
			this.method = requireNonNull(method);
			this.result = defaultValue(method.getReturnType());
		}

		/**
		 * @return Number of invocations
		 */
		public int count() {
			return count;
		}

		/**
		 * @return Whether this method has been invoked at least once
		 * @see #count()
		 */
		public boolean isInvoked() {
			return count > 0;
		}

		/**
		 * @return Most recent arguments
		 */
		public List<Object> arguments() {
			if(arguments == null) {
				return List.of();
			}
			else {
				return List.of(arguments);
			}
		}

		/**
		 * Mocks the return value of this method (including concrete implementations).
		 * @param result Method return value
		 * @throws IllegalArgumentException if this is a {@code void} method
		 * @throws NullPointerException if {@link #result} is {@code null} and the return type is primitive
		 * @throws ClassCastException if the return type is invalid for this method
		 */
		public void result(Object result) {
			final var type = method.getReturnType();
			if(type == void.class) {
				throw new IllegalArgumentException("Cannot mock the return value of a void method: " + this);
			}

			if(result == null) {
				if(type .isPrimitive()) {
					throw new NullPointerException("Primitive return value cannot be null: " + this);
				}
			}
			else {
				if(type.isPrimitive()) {
					final Class<?> expected = WRAPPERS.get(result.getClass());
					if((expected == null) || (expected != type)) {
						throw new ClassCastException("Invalid return wrapper %s for mocked method %s".formatted(result, this));
					}
				}
				else {
					if(!type.isAssignableFrom(result.getClass())) {
						throw new ClassCastException("Invalid return value %s for mocked method %s".formatted(result, this));
					}
				}
			}

			this.result = result;
		}

		/**
		 * Sets this method to fail with the given exception (including concrete implementations).
		 * @param exception Exception to throw
		 */
		public void fail(Throwable exception) {
			this.exception = exception;
		}

		/**
		 * Invokes this method.
		 * @param arguments Arguments
		 * @return Return value
		 * @throws Throwable if an exception is configured
		 */
		private Object invoke(Object[] arguments) throws Throwable {
			// Count number of invocations
			++count;
			this.arguments = arguments;

			// Throw if configured to fail
			if(exception != null) {
				throw exception;
			}

			// Delegate or return mocked result
			if((concrete == null) || (result != null)) {
				return result;
			}
			else {
				return concrete.invoke(arguments);
			}
		}

		@Override
		public String toString() {
			final Method delegate = concrete == null ? method : concrete.method;
			final boolean fail = Objects.nonNull(exception);
			return String.format("ProxyMethod[count=%d fail=%b method=%s]", count, fail, delegate);
		}
	}

	private static final Map<Class<?>, Object> PRIMITIVES = Map.of(
			boolean.class,		false,
			byte.class,			(byte) 0,
			char.class,			(char) 0,
			short.class,		(short) 0,
			int.class,			0,
			long.class,			0L,
			float.class,		0f,
			double.class,		0.0
	);

	private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
			Boolean.class,		boolean.class,
			byte.class,			byte.class,
			Character.class,	char.class,
			Short.class,		short.class,
			Integer.class,		int.class,
			Long.class,			long.class,
			Float.class,		float.class,
			Double.class,		double.class
	);

	private static Object defaultValue(Class<?> type) {
		final Class<?> actual = WRAPPERS.getOrDefault(type, type);
		return PRIMITIVES.get(actual);
	}
}
