package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * The <i>native library builder</i> constructs a proxy implementation of a native library using FFM.
 * <p>
 * A {@link NativeMethod} is generated for each public, non-static method of a given API.
 * The method parameters and return type are mapped to the corresponding transformers via the provided {@link Registry}.
 * <p>
 * The {@link #setReturnValueHandler(Consumer)} and {@link #setReturnValueHandler(IntConsumer)} can be used to configure validation or logging of method return values.
 * <p>
 * @see Transformer
 * @see NativeMethod
 * @author Sarge
 */
public class NativeLibraryBuilder {
	private final SymbolLookup lookup;
	private final ClassLoader loader = this.getClass().getClassLoader(); // TODO - mutable?
	private final Registry registry;
	private Consumer<Object> check = _ -> { /* Ignored */ };

	// TODO
	public NativeLibraryBuilder(SymbolLookup lookup, Registry registry) {
		this.lookup = requireNonNull(lookup);
		this.registry = requireNonNull(registry);
	}

	// TODO
	public NativeLibraryBuilder(String name, Registry registry) {
		final SymbolLookup lookup = SymbolLookup.libraryLookup(name, Arena.ofAuto());
		this(lookup, registry);
	}

	/**
	 * Configures a validation handler for native method return values.
	 * The default implementation does nothing.
	 * @param handler Return value handler
	 */
	public void setReturnValueHandler(Consumer<Object> handler) {
		this.check = requireNonNull(handler);
	}

	/**
	 * Helper - Configures a validation handler for native methods that return an integer success code.
	 * Non-integer or {@code null} return values are ignored.
	 * @param handler Success code handler
	 * @see #setReturnValueHandler(Consumer)
	 */
	public void setReturnValueHandler(IntConsumer handler) {
		final Consumer<Object> adapter = result -> {
			if(result instanceof Integer code) {
				handler.accept(code);
			}
		};
		setReturnValueHandler(adapter);
	}

	/**
	 * Constructs a proxy implementation the given native API.
	 * @param <T> API type
	 * @param api API
	 * @return Native proxy
	 * @throws IllegalArgumentException if {@link #api} is not an interface
	 * @throws IllegalArgumentException if any method cannot be found in the native library
	 * @throws IllegalArgumentException if the return type or any parameter of the native method are unsupported
	 */
	public <T> T build(Class<T> api) {
		if(!api.isInterface()) {
			throw new IllegalArgumentException("Native API must be specified by an interface");
		}

		final Map<Method, NativeMethod> methods = methods(api);
		return proxy(api, methods);
	}

	/**
	 * Builds the declared native methods of the given API.
	 * @param api API
	 * @return Native methods
	 */
	private Map<Method, NativeMethod> methods(Class<?> api) {
		return Arrays
    			.stream(api.getMethods())
    			.filter(NativeLibraryBuilder::isNativeMethod)
    			.collect(toMap(Function.identity(), this::build));
	}

	/**
	 * Builds a native method for the given API method.
	 * @param method API method
	 * @return Native method
	 * @throws IllegalArgumentException if the native method cannot be found
	 * @throws IllegalArgumentException if the return type or parameters are not supported
	 */
	public NativeMethod build(Method method) {
		try {
			return buildLocal(method);
		}
		catch(Exception e) {
			final String type = method.getDeclaringClass().getSimpleName();
			final String name = String.format("%s::%s", type, method.getName());
			throw new IllegalArgumentException("Error building native method: " + name, e);
		}
	}

	private NativeMethod buildLocal(Method method) {
		// Lookup method symbol
		final MemorySegment symbol = lookup
				.find(method.getName())
				.orElseThrow(() -> new IllegalArgumentException("Unknown native method"));

		// Init native method
		final var builder = new NativeMethod.Builder(registry)
				.address(symbol)
				.returns(method.getReturnType());

		// Configure signature
		for(Parameter p : method.getParameters()) {
			final boolean returned = Objects.nonNull(p.getAnnotation(Returned.class));
			builder.parameter(p.getType(), returned);
		}

		return builder.build();
	}

	/**
	 * @return Whether the given API method is a suitable native method
	 */
	private static boolean isNativeMethod(Method method) {
		final int modifiers = method.getModifiers();
		return !Modifier.isStatic(modifiers);
	}

	/**
	 * Creates a proxy implementation of the given API.
	 * @param <T> API type
	 * @param api			API definition
	 * @param methods		Native methods
	 * @return Proxy implementation
	 */
	@SuppressWarnings("unchecked")
	private <T> T proxy(Class<T> api, Map<Method, NativeMethod> methods) {
		final var handler = new InvocationHandler() {
    		@Override
    		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    			final NativeMethod delegate = methods.get(method);
    			final Object result = delegate.invoke(args);
    			check.accept(result);
    			return result;
    		}
    	};

    	return (T) Proxy.newProxyInstance(loader, new Class<?>[]{api}, handler);
	}
}
