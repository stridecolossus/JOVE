package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import org.sarge.jove.foreign.NativeMethod.NativeParameter;

/**
 * The <i>native library builder</i> constructs a proxy implementation of a native library based on FFM.
 * <p>
 * A {@link NativeMethod} is generated for each public, non-static method of a given API.
 * The method parameters and return type are mapped to the corresponding transformers via the provided {@link Registry}.
 * <p>
 * The {@link #handler(Consumer)} method can be used to configure validation or logging of native return values.
 * <p>
 * @see NativeMethod
 * @author Sarge
 */
public class NativeLibraryFactory {
	/**
	 * Return value handler that does nothing.
	 */
	public static final Consumer<Object> IGNORE = _ -> {
		// Ignored
	};

	private final Linker linker = Linker.nativeLinker();
	private final SymbolLookup lookup;
	private final Registry registry;
	private Consumer<Object> returns = IGNORE;
	/**
	 * Constructor.
	 * @param lookup		Native lookup service
	 * @param registry		Registered transformers
	 */
	public NativeLibraryFactory(SymbolLookup lookup, Registry registry) {
		this.lookup = requireNonNull(lookup);
		this.registry = requireNonNull(registry);
	}

	/**
	 * Convenience constructor.
	 * @param name			Name of the native library
	 * @param registry		Transformer registry
	 */
	public NativeLibraryFactory(String name, Registry registry) {
		@SuppressWarnings("resource")
		final SymbolLookup lookup = SymbolLookup.libraryLookup(name, Arena.ofAuto());
		this(lookup, registry);
	}

	/**
	 * Configures the handler for native method return values.
	 * @param handler Return value handler
	 * @see #IGNORE
	 */
	public void handler(Consumer<Object> handler) {
		this.returns = requireNonNull(handler);
	}

	/**
	 * Constructs a proxy implementation of the given native API.
	 * @param api API interface(s)
	 * @return Native proxy
	 * @throws IllegalArgumentException if any {@link #api} is not an interface
	 * @throws IllegalArgumentException if any API method is not present in the native library
	 * @throws IllegalArgumentException if the return type or any parameter of an API method are unsupported
	 */
	public Object build(List<Class<?>> api) {
		// Enumerate API methods
		final Map<Method, NativeMethod> methods = api
    			.stream()
    			.peek(NativeLibraryFactory::validate)
    			.map(Class::getMethods)
    			.flatMap(Arrays::stream)
    			.filter(NativeLibraryFactory::isNativeMethod)
    			.collect(toMap(Function.identity(), this::build));

		// Delegate API calls to the underlying native methods
		final var handler = new InvocationHandler() {
    		@Override
    		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    			final NativeMethod delegate = methods.get(method);
    			final Object result = delegate.invoke(args);
    			returns.accept(result);
    			return result;
    		}
    	};

    	// Create proxy implementation
		final ClassLoader loader = this.getClass().getClassLoader();
		final var interfaces = api.toArray(Class<?>[]::new);
    	return Proxy.newProxyInstance(loader, interfaces, handler);
	}

	/**
	 * Checks that a declared API is indeed an interface.
	 */
	private static void validate(Class<?> api) {
		if(!api.isInterface()) {
			throw new IllegalArgumentException("Native API must be specified by an interface: " + api);
		}
	}

	/**
	 * @return Whether the given API method is an appropriate native method
	 */
	private static boolean isNativeMethod(Method method) {
		final int modifiers = method.getModifiers();
		return Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers);
	}

	/**
	 * Builds a native method for the given API method.
	 * @param method API method
	 * @return Native method
	 */
	private NativeMethod build(Method method) {
		try {
			return buildLocal(method);
		}
		catch(Exception e) {
			final Class<?> type = method.getDeclaringClass();
			final String reason = String.format("%s in %s::%s", e.getMessage(), type, method.getName());
			throw new IllegalArgumentException(reason, e);
		}
	}

	@SuppressWarnings("rawtypes")
	private NativeMethod buildLocal(Method method) {
		// Lookup method address
		final MemorySegment symbol = lookup
				.find(method.getName())
				.orElseThrow(() -> new IllegalArgumentException("Unknown native method: " + method));

		// Map return value
		final Transformer returns = returns(method);

		// Map parameters
		final List<NativeParameter> parameters = Arrays
				.stream(method.getParameters())
				.map(this::parameter)
				.toList();

		// Link native method
		final FunctionDescriptor descriptor = NativeMethod.descriptor(returns, parameters);
		MethodHandle handle = linker.downcallHandle(descriptor).bindTo(symbol);
		return new NativeMethod(handle, returns, parameters);
	}

	/**
	 * Determines the transformer for the return value of the given method.
	 * @param method Method
	 * @return Return type transformer or {@code null} for a {@code void} method
	 */
	@SuppressWarnings("rawtypes")
	private Transformer returns(Method method) {
		final Class<?> type = method.getReturnType();

		if(type == void.class) {
			return null;
		}

		return registry
				.transformer(type)
				.orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + method));
	}

	/**
	 * Determines the native transformer for the given method parameter.
	 * @param parameter Method parameter
	 * @return Native parameter
	 */
	private NativeParameter parameter(Parameter parameter) {
		@SuppressWarnings("rawtypes")
		final Transformer transformer = registry
				.transformer(parameter.getType())
				.orElseThrow(() -> new IllegalArgumentException("Unsupported parameter type: " + parameter));

		final boolean updated = parameter.isAnnotationPresent(Updated.class);

		return new NativeParameter(transformer, updated);
	}
}
