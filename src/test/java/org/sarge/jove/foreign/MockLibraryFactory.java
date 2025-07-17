package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.util.WrapperType;

/**
 *
 * @author Sarge
 */
public class MockLibraryFactory {
	/**
	 *
	 */
	public static class MockedMethod implements DelegateMethod {
		private final Method method;
		private int count;
		private int failures;
		private Object result;
		private Throwable fail;
		//private DelegateMethod delegate;
		private Method delegate;
		private Object proxy;

		/**
		 * Constructor.
		 * @param method API method
		 */
		private MockedMethod(Method method) {
			this.method = requireNonNull(method);
			this.result = init();
		}

		/**
		 * @return Number of invocations
		 */
		public int count() {
			return count;
		}

		/**
		 * Sets the mocked return value for this mock.
		 * @param result Return value or {@code null} to revert to the default value for this method
		 * @throws IllegalArgumentException if {@link #result} does not match the return type of this method
		 */
		public void returns(Object result) {
			if(result == null) {
				this.result = init();
			}
			else {
				final Class<?> expected = method.getReturnType();
				if(expected == void.class) {
					throw new IllegalArgumentException("Cannot mock the return value of a void method: " + method);
				}

				final Class<?> actual = result.getClass();
				if(expected.isPrimitive()) {
					final WrapperType wrapper = WrapperType.PRIMITIVES.get(expected);
					if(actual != wrapper.wrapper()) {
						throw new IllegalArgumentException("Invalid boxed return type %s for method %s".formatted(actual, method));
					}
				}
				else {
					if(!expected.isAssignableFrom(actual)) {
						throw new IllegalArgumentException("Invalid return type %s for method %s".formatted(actual, method));
					}
				}
				this.result = result;
			}
		}

		/**
		 * @return Default return value of this mock
		 */
		private Object init() {
			final Class<?> type = method.getReturnType();
			if(type == void.class) {
				return null;
			}
			if(type.isPrimitive()) {
				return WrapperType.PRIMITIVES.get(type).value();
			}
			else {
				return null;
			}
		}

		/**
		 * @return Number of failed invocations
		 * @see #fail(Throwable)
		 */
		public int failures() {
			return failures;
		}

		/**
		 * Sets this mock to fail with the given exception.
		 * @param fail Failure to throw
		 * @see #failures()
		 */
		public void fail(Throwable fail) {
			this.fail = fail;
		}

		/**
		 * Implements this mock to delegate to the given method.
		 * @param delegate Delegate invocation or {@code null} to revert to mocked behaviour
		 */
		public void implement(DelegateMethod delegate) {
//			this.delegate = delegate;
		}


		public MockedMethod implement(Object proxy, Class<?> type) {
			try {
				this.proxy = proxy;
				delegate = type.getMethod(method.getName(), method.getParameterTypes());
			}
			catch(Exception e) {
				throw new IllegalArgumentException("Unknown method %s in %s".formatted(method, type), e);
			}
			return this;
		}



		@Override
		public Object invoke(Object[] args) throws Throwable {
			// Count number of invocations
			++count;

			// Throw exception if configured and count number of failures
			if(Objects.nonNull(fail)) {
				++failures;
				throw fail;
			}

			// Delegate to the mocked implementation if configured
			if(Objects.nonNull(delegate)) {
				return delegate.invoke(proxy, args);
			}

//			// TODO - some sort of parameter interceptor/listener?
//			for(Object arg : args) {
//				if(arg instanceof Pointer p) {
//					p.set(new Handle(2));
//				}
//			}

			// Otherwise return the mocked result
			return result;
		}

		@Override
		public int hashCode() {
			return method.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof MockedMethod that) &&
					this.method.equals(that.method);
		}

		@Override
		public String toString() {
			return String.format("%s[%d/%d]", method, failures, count);
		}
	}

	/**
	 *
	 */
	private record MethodKey(String name, List<Class<?>> signature) {
		/**
		 * Constructor.
		 */
		private MethodKey(String name, Class<?>[] parameters) {
			this(name, List.of(parameters));
		}

		/**
		 * Constructor given reflected method.
		 */
		private MethodKey(Method method) {
			this(method.getName(), method.getParameterTypes());
		}

		private Predicate<Method> filter() {
			final Predicate<Method> filter = m -> m.getName().equals(name);
			if(signature.isEmpty()) {
				return filter;
			}
			else {
				return filter.and(m -> signature.equals(List.of(m.getParameterTypes())));
			}
		}
	}

	private final Class<?> api;
	private final Map<MethodKey, MockedMethod> methods = new HashMap<>();

	/**
	 * Constructor.
	 * @param api Mocked API
	 */
	public MockLibraryFactory(Class<?> api) {
		this.api = requireNonNull(api);
	}

	/**
	 * Retrieves a mocked method.
	 * @param name				Method name
	 * @param parameters		Parameter types
	 * @return Mocked method
	 * @throws IllegalArgumentException if the method is not present in the API
	 */
	public MockedMethod get(String name, Class<?>... parameters) {
		final MethodKey key = new MethodKey(name, parameters);
		final MockedMethod prev = methods.get(key);
		if(prev == null) {
			final Method method = find(key);
			return methods.computeIfAbsent(new MethodKey(method), _ -> new MockedMethod(method));
		}
		else {
			return prev;
		}
	}

	private Method find(MethodKey key) {
		final List<Method> found = Arrays
				.stream(api.getMethods())
				.filter(key.filter())
				.toList();

		return switch(found.size()) {
			case 1 -> found.get(0);
			case 0 -> throw new IllegalArgumentException("Unknown method: " + key);
			default -> throw new IllegalArgumentException("Ambiguous method: " + key);
		};
	}

	/**
	 * Creates a mock implementation of the API.
	 * @param <T> Mocked type
	 * @return Mock implementation
	 * @throws ClassCastException if the api does not implement {@link T}
	 */
	@SuppressWarnings("unchecked")
	public <T> T proxy() {
		final var handler = new InvocationHandler() {
        	@Override
    		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        		final var key = new MethodKey(method);
        		final DelegateMethod delegate = methods.computeIfAbsent(key, _ -> new MockedMethod(method));
        		return delegate.invoke(args);
        	}
    	};

		final ClassLoader loader = DelegateMethod.class.getClassLoader();
		final Class<?>[] interfaces = {api};
		return (T) Proxy.newProxyInstance(loader, interfaces, handler);
	}
}
