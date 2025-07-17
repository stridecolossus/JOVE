package org.sarge.jove.foreign;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * A <i>delegate method</i> defines the target of a proxy API.
 * <p>
 * The {@link #proxy(Class, Map, Consumer)} factory creates a proxy implementation of a given API implemented by delegate methods.
 * <p>
 * @author Sarge
 */
@FunctionalInterface
public interface DelegateMethod {
	/**
	 * Invokes this delegate method.
	 * @param args Optional array of arguments
	 * @return Return value
	 */
	Object invoke(Object[] args) throws Throwable;

	/**
	 * Builds a proxy implementation of an API that dispatches method invocations to the given delegates.
	 * @param <T> API class
	 * @param api			API to be proxied
	 * @param methods		Delegate methods
	 * @param returns		Optional return value handler
	 * @return Proxy implementation
	 */
	@SuppressWarnings("unchecked")
	static <T> T proxy(Collection<Class<?>> api, Map<Method, ? extends DelegateMethod> methods, Consumer<Object> returns) {
		// Create handler that delegates to the underlying methods
		final var handler = new InvocationHandler() {
			// Empty return value handler
			private static final Consumer<Object> IGNORE = _ -> {
				// Ignored
			};

			private Consumer<Object> consumer = Objects.requireNonNullElse(returns, IGNORE);

        	@Override
    		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        		final DelegateMethod delegate = methods.get(method);
        		final Object result = delegate.invoke(args);
        		consumer.accept(result);
        		return result;
        	}
    	};

    	// Create proxy
		final ClassLoader loader = DelegateMethod.class.getClassLoader();
		final Class<?>[] interfaces = api.toArray(Class[]::new); //{api};
		return (T) Proxy.newProxyInstance(loader, interfaces, handler);
	}
}
