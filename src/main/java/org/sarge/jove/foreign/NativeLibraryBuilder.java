package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.Logger;

/**
 *
 * TODO
 *
 * The <i>native library builder</i> constructs a proxy implementation of a native library implemented using FFM.
 * <p>
 * The {@link #build(SymbolLookup, Class)} method constructs a proxy implementation of a given native API.
 * Each public, non-static method of the API generates a {@link NativeMethod} with a method handle retrieved from the given symbol lookup.
 * The method parameters and optional return type are mapped to the corresponding native transformers via the {@link TransformerRegistry} provided in the constructor.
 * <p>
 *
 * TODO
 *
 * The {@link #setReturnHandler(Consumer)} and {@link #setIntegerReturnHandler(IntConsumer)} methods can be used to configure validation or logging of native method returns values.
 * <p>
 * @author Sarge
 */
public class NativeLibraryBuilder {
	private static final Logger LOG = Logger.getLogger(NativeLibraryBuilder.class.getName());

	private final SymbolLookup lookup;
	private final ClassLoader loader = this.getClass().getClassLoader(); // TODO - mutable?
	private final Registry registry;
	private Consumer<Object> check = result -> { /* Ignored */ };

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
	 * Sets a validation handler for native method return values.
	 * The default implementation does nothing.
	 * @param handler Return value handler
	 */
	public void setReturnValueHandler(Consumer<Object> handler) {
		this.check = requireNonNull(handler);
	}

	/**
	 * Helper - Sets a validation handler for native methods that return an integer success code.
	 * Non-integer or {@code null} return values are ignored.
	 * @param handler Success code handler
	 * @see #setReturnValueHandler(Consumer)
	 */
	public void setReturnValueHandler(IntConsumer handler) {
		this.check = result -> {
			if(result instanceof Integer code) {
				handler.accept(code);
			}
		};
	}

	/**
	 * Constructs a native method proxy for the given API.
	 * @param <T> API type
	 * @param api API
	 * @return Native proxy
	 * @throws IllegalArgumentException if {@link #api} is not an interface
	 * @throws IllegalArgumentException if any method cannot be found in the native library
	 * @throws IllegalArgumentException if the return type or parameters of a native method are not unsupported
	 */
	public <T> T build(Class<T> api) {
		if(!api.isInterface()) {
			throw new IllegalArgumentException("Native API must be specified by an interface: " + api);
		}

		LOG.info("Building API: " + api);
		final Map<Method, NativeMethod> methods = methods(api);
		return proxy(api, methods);
	}

	/**
	 * Builds a native method instance for the declared methods of the given API.
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
			// Init native method
			LOG.info("Building native method: " + format(method));
			final var builder = new NativeMethod.Builder(registry);

			// Configure method signature
			for(Parameter p : method.getParameters()) {
				final boolean returned = Objects.nonNull(p.getAnnotation(Returned.class));
				builder.parameter(p.getType(), returned);
			}

			// Construct native method
			return builder
        			.address(lookup(method))
        			.returns(method.getReturnType())
        			.build();
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Error building native method: " + format(method), e);
		}
	}

	private static String format(Method method) {
		final String type = method.getDeclaringClass().getSimpleName();
		return String.format("%s::%s", type, method.getName());
	}

	/**
	 * Looks up the address of a native method.
	 * @param method API method
	 * @return Native method address
	 * @throws IllegalArgumentException if the native method cannot be found
	 */
	private MemorySegment lookup(Method method) {
		return lookup
				.find(method.getName())
				.orElseThrow(() -> new IllegalArgumentException("Unknown native method"));
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
