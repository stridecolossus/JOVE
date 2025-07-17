package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * The <i>native library factory</i> constructs a proxy implementation of a native library using FFM.
 * <p>
 * A {@link NativeMethod} is generated for each public, non-static method of a given API.
 * The method parameters and return type are mapped to the corresponding transformers via the provided {@link Registry}.
 * <p>
 * The {@link #setReturnValueHandler(Consumer)} method can be used to configure validation or logging of native return values.
 * <p>
 * @see Transformer
 * @see NativeMethod
 * @author Sarge
 */
public class NativeLibraryFactory {
	private final SymbolLookup lookup;
	private final Linker linker = Linker.nativeLinker();
	private final Registry registry;
	private Consumer<Object> check = _ -> { /* Ignored */ };

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
	 * @param registry		Registered transformers
	 */
	public NativeLibraryFactory(String name, Registry registry) {
		@SuppressWarnings("resource")
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
		final InvocationHandler handler = handler(methods);
		return proxy(api, handler);
	}

	/**
	 * Builds the lookup table of native methods by reflecting the given API.
	 * @param api API
	 * @return Native methods
	 */
	private Map<Method, NativeMethod> methods(Class<?> api) {
		return Arrays
    			.stream(api.getMethods())
    			.filter(NativeLibraryFactory::isNativeMethod)
    			.collect(toMap(Function.identity(), this::build));
	}

	/**
	 * @return Whether the given API method is an appropriate native method
	 */
	private static boolean isNativeMethod(Method method) {
		final int modifiers = method.getModifiers();
		return Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers);
	}

	/**
	 * Creates an invocation handler that delegates API calls to the underlying native methods.
	 * @param methods Native methods
	 * @return Delegate handler
	 */
	private InvocationHandler handler(Map<Method, NativeMethod> methods) {
		return new InvocationHandler() {
    		@Override
    		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    			final NativeMethod delegate = methods.get(method);
    			final Object result = delegate.invoke(args);
    			check.accept(result);
    			return result;
    		}
    	};
	}

	/**
	 * Creates a proxy implementation of an API that delegates method calls to the given handler.
	 * @param <T> API type
	 * @param api			API definition
	 * @param handler		Delegation handler
	 * @return Proxy implementation
	 */
	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> api, InvocationHandler handler) {
		final ClassLoader loader = api.getClassLoader();
    	return (T) Proxy.newProxyInstance(loader, new Class<?>[]{api}, handler);
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
			final String reason = String.format("%s in %s::%s", e.getMessage(), type.getSimpleName(), method.getName());
			throw new IllegalArgumentException(reason, e);
		}
	}

	private NativeMethod buildLocal(Method method) {
		// Lookup method address
		final MemorySegment symbol = lookup
				.find(method.getName())
				.orElseThrow(() -> new IllegalArgumentException("Unknown native method"));

		// Map return value
		final Transformer returns = registry
				.transformer(method.getReturnType())
				.orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + method.getReturnType()));

		// Map parameters
		final List<Transformer> parameters = Arrays
				.stream(method.getParameters())
				.map(this::parameter)
				.toList();

		// Link native method
		final FunctionDescriptor descriptor = NativeMethod.descriptor(returns, parameters);
		final MethodHandle handle = linker.downcallHandle(descriptor).bindTo(symbol);

		// Create native method
		return new NativeMethod(handle, returns, parameters);
	}

	/**
	 * Determines the native transformer for the given method parameter.
	 * @param parameter Method parameter
	 * @return Transformer
	 */
	private Transformer parameter(Parameter parameter) {
		// Lookup transformer for this type
		final Transformer transformer = registry
				.transformer(parameter.getType())
				.orElseThrow(() -> new IllegalArgumentException("Unsupported parameter type: " + parameter));

		final boolean reference = parameter.isAnnotationPresent(Returned.class);
		if(reference) {
			// TODO - handle conversion to by-reference transformer
		}

		return transformer;
	}
}
