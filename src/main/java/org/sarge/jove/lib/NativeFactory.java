package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

/**
 * The <i>native factory</i> is used to build a proxy API for a native library.
 * @author Sarge
 */
public class NativeFactory {
	private final NativeMapperRegistry registry;
	private final ClassLoader loader = this.getClass().getClassLoader(); // TODO - mutable?

	/**
	 * Constructor.
	 * @param registry Registry of supported native mappers
	 */
	public NativeFactory(NativeMapperRegistry registry) {
		this.registry = requireNonNull(registry);
	}

	/**
	 * Constructs a proxy implementation for the given native library.
	 * @param <T> Native API
	 * @param lookup		Native library lookup
	 * @param api			Native API
	 * @return Proxy implementation
	 * @throws IllegalArgumentException if the given {@link #api} is not an interface
	 * @throws IllegalArgumentException if a native method is unsupported
	 */
	public <T> T build(SymbolLookup lookup, Class<T> api) {
		if(!api.isInterface()) throw new IllegalArgumentException("Native API must be specified by an interface: " + api);

		// Init native method builder
		final var builder = new NativeMethod.Factory(lookup, registry);

		// Build native methods for this API
		final Map<Method, NativeMethod> methods = Arrays
				.stream(api.getMethods())
				.filter(NativeFactory::isNativeMethod)
				.collect(toMap(Function.identity(), builder::build));

		// Create handle to delegate method invocations to the relevant native method
		final var handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				final NativeMethod delegate = methods.get(method);
				try(final Arena arena = Arena.ofConfined()) {
					return delegate.invoke(args, arena);
				}
			}
		};

		// Create proxy API
		@SuppressWarnings("unchecked")
		final T proxy = (T) Proxy.newProxyInstance(loader, new Class<?>[]{api}, handler);

		return proxy;
	}

	/**
	 * Constructs a proxy implementation for the given native library.
	 * @param <T> Native API
	 * @param name		Native library name
	 * @param api		Native API
	 * @see #build(SymbolLookup, Class)
	 */
	public <T> T build(String name, Class<T> api) {
		final Arena arena = Arena.ofAuto(); // TODO - context
//		try(final Arena arena = Arena.ofConfined()) {
			final var lookup = SymbolLookup.libraryLookup(name, arena);
			return build(lookup, api);
//		}
	}

	/**
	 * @return Whether the given API method is a suitable native method
	 */
	private static boolean isNativeMethod(Method method) {
		final int modifiers = method.getModifiers();
		return !Modifier.isStatic(modifiers);
	}
}
