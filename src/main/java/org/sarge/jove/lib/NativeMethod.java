package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * A <i>native method</i> abstracts a native method handle.
 * TODO - marshalling
 * @author Sarge
 */
public class NativeMethod {
	private final Method method;
	private final MethodHandle handle;
	private final NativeMapper[] signature;
	private final NativeMapper returnMapper;

	/**
	 * Constructor.
	 * @param method			API method
	 * @param handle 			Native method handle
	 * @param signature			Parameter mappers
	 * @param returnMapper		Optional return type mapper
	 * @throws IllegalArgumentException if the {@link #signature} or {@link #returnMapper} does not match the native method
	 */
	NativeMethod(Method method, MethodHandle handle, List<NativeMapper> signature, NativeMapper returnMapper) {
		if(signature.size() != method.getParameterCount()) throw new IllegalArgumentException("Mismatched method signature");
		if((method.getReturnType() == void.class) ^ (returnMapper == null)) throw new IllegalArgumentException("Mismatched return type mapper");

		this.method = requireNonNull(method);
		this.handle = requireNonNull(handle);
		this.signature = signature.toArray(NativeMapper[]::new);
		this.returnMapper = returnMapper;
	}

	// https://dev.java/learn/introduction_to_method_handles/

	/**
	 * Invokes this native method with the given arguments.
	 * TODO - marshalling
	 * @param args 		Arguments
	 * @param arena		Arena
	 * @return Return value or {@code null} for a {@code void} method
	 * @throws RuntimeException if the native method fails
	 */
	public Object invoke(Object[] args, Arena arena) {
		final Object[] actual = marshal(args, arena);
		final Object result = execute(actual);
		return marshalReturnValue(result);
	}

	/**
	 * Maps arguments to the native representation.
	 * @param args			Arguments
	 * @param arena			Arena
	 * @return Mapped arguments
	 */
	@SuppressWarnings("unchecked")
	private Object[] marshal(Object[] args, Arena arena) {
		if(args == null) {
			return null;
		}

		final Object[] mapped = Arrays.copyOf(args, args.length);
		final Parameter[] parameters = method.getParameters();
		for(int n = 0; n < mapped.length; ++n) {
			if(signature[n] instanceof NativeTypeConverter c) {
				mapped[n] = c.toNative(args[n], parameters[n].getType(), arena);
			}
		}

		return mapped;
	}

	/**
	 * Executes this native method with the given mapped arguments.
	 * @param args Mapped arguments
	 * @return Return value
	 */
	private Object execute(Object[] args) {
		try {
			return handle.invokeWithArguments(args);
		}
		catch(Throwable e) {
			throw new RuntimeException("Error invoking native method: " + this, e);
		}
	}

	/**
	 * Maps the return value of this method.
	 * @param value Return value
	 * @return Mapped return value
	 */
	@SuppressWarnings("unchecked")
	private Object marshalReturnValue(Object value) {
		if(returnMapper instanceof NativeTypeConverter converter) {
			return converter.fromNative(value, method.getReturnType());
		}
		else {
			return value;
		}
	}

	@Override
	public int hashCode() {
		return method.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeMethod that) &&
				this.method.equals(that.method);
	}

	@Override
	public String toString() {
		return String.format("NativeMethod[%s]", method);
	}

	/**
	 * Factory for native methods.
	 */
	public static class Factory {
		private final Linker linker = Linker.nativeLinker();		// TODO - from NativeFactory? mutable?
		private final SymbolLookup lookup;
		private final NativeMapperRegistry registry;

		/**
		 * Constructor.
		 * @param lookup		Symbol lookup
		 * @param registry		Mapper registry
		 */
		public Factory(SymbolLookup lookup, NativeMapperRegistry registry) {
			this.lookup = requireNonNull(lookup);
			this.registry = requireNonNull(registry);
		}

		/**
		 * Builds the native method wrapper for the given API method.
		 * @param method API method
		 * @return Native method
		 * @throws IllegalArgumentException for an unknown method or an unsupported parameter or return type
		 */
		public NativeMethod build(Method method) {
			final MemorySegment symbol = symbol(method);

			final List<NativeMapper> signature = Arrays
					.stream(method.getParameters())
					.map(this::mapper)
					.toList();

			// Map return type
			final NativeMapper returnMapper = returnMapper(method);

			// Build the native method handle
			final FunctionDescriptor descriptor = descriptor(signature, returnMapper);
			final MethodHandle handle = linker.downcallHandle(symbol, descriptor);

			// Create native method wrapper
			return new NativeMethod(method, handle, signature, returnMapper);
		}

		/**
		 * Looks up the method symbol from the native library.
		 * @param method API method
		 * @return Native method symbol
		 * @throws IllegalArgumentException if the given method is not present in the API
		 */
		private MemorySegment symbol(Method method) {
			return lookup
        			.find(method.getName())
        			.orElseThrow(() -> new IllegalArgumentException("Unknown method: " + method));
		}

		/**
		 * Looks up the native mapper for the given parameter.
		 */
		private NativeMapper mapper(Parameter parameter) {
			return map(parameter.getType(), () -> new IllegalArgumentException(String.format("Unsupported parameter type %s in %s", parameter.getType(), parameter.getDeclaringExecutable())));
		}

		/**
		 * Looks up the native mapper for the optional method return type.
		 */
		private NativeMapper returnMapper(Method method) {
			final Class<?> returnType = method.getReturnType();
			if(returnType == void.class) {
				return null;
			}
			else {
				return map(returnType, () -> new IllegalArgumentException(String.format("Unsupported return type %s for %s", returnType, method)));
			}
		}

		/**
		 * Looks up the native mapper for the given Java type.
		 * @param type		Java type
		 * @param error		Exception supplier
		 * @return Native mapper
		 * @throws IllegalArgumentException if the given type is not supported
		 */
		private NativeMapper map(Class<?> type, Supplier<IllegalArgumentException> error) {
			return registry.mapper(type).orElseThrow(error);
		}

		/**
		 * Builds the native function descriptor for the given mapped method signature.
		 * @param signature			Method signature
		 * @param returnType		Return type
		 * @return Native function descriptor
		 */
		private static FunctionDescriptor descriptor(List<NativeMapper> signature, NativeMapper returnType) {
			// Map method signature to parameter layouts
			final MemoryLayout[] layouts = signature
					.stream()
					.map(NativeMapper::layout)
					.toArray(MemoryLayout[]::new);

			// Determine native function descriptor
			if(returnType == null) {
				return FunctionDescriptor.ofVoid(layouts);
			}
			else {
				return FunctionDescriptor.of(returnType.layout(), layouts);
			}
		}
	}
}
