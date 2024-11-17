package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

/**
 * The <i>native factory</i> is used to instantiate a native library.
 *
 * construct a proxy API for a native library.
 * <p>
 * The {@link #build(SymbolLookup, Class)} method constructs a proxy implementation of a given native API.
 * Each public, non-static method of the API generates a {@link NativeMethod} with a method handle retrieved from the given symbol lookup.
 * The method parameters and optional return type are mapped to the corresponding native mappers via the {@link NativeMapperRegistry} provided in the constructor.
 * <p>
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
	 * Constructs a proxy implementation for the given native library.
	 * @param <T> Native API
	 * @param lookup		Native library lookup
	 * @param api			Native API
	 * @return Proxy implementation
	 * @throws IllegalArgumentException if the given {@link #api} is not an interface
	 * @throws IllegalArgumentException if a native method is unsupported
	 */
	public <T> T build(SymbolLookup lookup, Class<T> api) {
		// Validate
		if(!api.isInterface()) throw new IllegalArgumentException("Native API must be specified by an interface: " + api);

		// Create factory instance
		final var instance = new Instance(lookup);

		// Build native methods for this API
		final Map<Method, NativeMethod> methods = Arrays
				.stream(api.getMethods())
				.filter(NativeFactory::isNativeMethod)
				.collect(toMap(Function.identity(), instance::build));

		// Delegate method invocations to the corresponding native method
		final var handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				final NativeMethod delegate = methods.get(method);
				final var context = new NativeContext(Arena.ofAuto(), registry);
				return delegate.invoke(args, context);
			}
		};

		// Create proxy API
		@SuppressWarnings("unchecked")
		final T proxy = (T) Proxy.newProxyInstance(loader, new Class<?>[]{api}, handler);

		return proxy;
	}

	/**
	 * Helper.
	 */
	private class Instance {
		private final SymbolLookup lookup;

		Instance(SymbolLookup lookup) {
			this.lookup = lookup;
		}

		/**
		 * Builds a native method from the given reflected API method.
		 */
		public NativeMethod build(Method method) {
			final MemorySegment address = address(method);
			try {
				return build(address, method);
			}
			catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("Error building native method: " + method, e);
			}
		}

		/**
		 * Looks up the native method symbol.
		 */
		private MemorySegment address(Method method) {
			return lookup
					.find(method.getName())
					.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown native method %s::%s", method.getDeclaringClass().getSimpleName(), method.getName())));
		}

		/**
		 * Constructs the native method.
		 */
		private NativeMethod build(MemorySegment address, Method method) {
			// Init method builder
			final var builder = new NativeMethod.Builder(registry)
					.address(address)
					.signature(method.getParameterTypes());

			// Set return type
			final Class<?> returnType = method.getReturnType();
			if(returnType != void.class) {
				builder.returns(returnType);
			}

			// Construct native method
			return builder.build();
		}
	}

	/**
	 * @return Whether the given API method is a suitable native method
	 */
	private static boolean isNativeMethod(Method method) {
		final int modifiers = method.getModifiers();
		return !Modifier.isStatic(modifiers);
	}
}
