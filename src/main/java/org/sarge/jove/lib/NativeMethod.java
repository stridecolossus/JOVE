package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;

/**
 * A <i>native method</i> composes a native method handle and the associated mappers.
 * @author Sarge
 */
public class NativeMethod {
	private final MethodHandle handle;
	private final NativeMapper<?>[] mappers;
	private final NativeMapper<?> returnMapper;

	/**
	 * Constructor.
	 * @param handle			Native method
	 * @param mappers			Parameter mappers
	 * @param returnMapper		Optional return type mapper
	 */
	NativeMethod(MethodHandle handle, List<? extends NativeMapper<?>> mappers, NativeMapper<?> returnMapper) {
		this.handle = requireNonNull(handle);
		this.mappers = mappers.toArray(NativeMapper[]::new);
		this.returnMapper = returnMapper;
	}

	/**
	 * Invokes this native method with the given parameters.
	 * TODO - marshalling
	 * @param args Arguments
	 * @return Return value
	 * @throws RuntimeException if the method fails
	 */
	public Object invoke(Object[] args) {
		final Object[] mapped = toNative(args);
		final Object value = execute(mapped);
		return fromNative(value);
	}

	/**
	 * Marshals the method arguments to native types.
	 */
	private Object[] toNative(Object[] args) {
		if(args == null) {
			return null;
		}

		final Object[] mapped = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			final var mapper = mappers[n];
			final var arg = mapper.toNative(args[n], null);
			mapped[n] = arg;
		}

		return mapped;
	}

	/**
	 * Invokes this native method.
	 */
	private Object execute(Object[] args) {
		try {
			return handle.invokeWithArguments(args);
		}
		catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Marshals the return value.
	 */
	private Object fromNative(Object value) {
    	if(returnMapper == null) {
    		assert value == null;
    		return null;
    	}
    	else {
    		return returnMapper.fromNative(value);
    	}
	}

	/**
	 * Builder for a native method.
	 */
	public static class Builder {
		private final SymbolLookup lookup;
		private final NativeContext context;

		/**
		 * Constructor.
		 * @param lookup		Symbol lookup
		 * @param context		Native context
		 */
		public Builder(SymbolLookup lookup, NativeContext context) {
			this.lookup = requireNonNull(lookup);
			this.context = requireNonNull(context);
		}

		/**
		 * Builds the native method wrapper for the given API method.
		 * @param method API method
		 * @return Native method
		 * @throws IllegalArgumentException for an unknown method or an unsupported parameter or return type
		 */
		public NativeMethod build(Method method) {
			// Lookup the method symbol
			final MemorySegment address = lookup
        			.find(method.getName())
        			.orElseThrow(() -> new IllegalArgumentException("Unknown method: " + method));

			// Map parameters
			final var mappers = Arrays
					.stream(method.getParameters())
					.map(this::mapper)
					.toList();

			// Map return type
			final NativeMapper<?> returnMapper = returnMapper(method);

			// Create method handle
			final FunctionDescriptor descriptor = descriptor(mappers, returnMapper);
			final MethodHandle handle = context.linker().downcallHandle(address, descriptor);

			// Builds native method wrapper
			return new NativeMethod(handle, mappers, returnMapper);
		}

		/**
		 * Looks up the native mapper for the given parameter.
		 */
		private NativeMapper<?> mapper(Parameter parameter) {
			return context
					.registry()
					.mapper(parameter.getType())
					.orElseThrow(() -> new IllegalArgumentException("Unsupported parameter type: " + parameter));
		}

		/**
		 * Looks up the native mapper for the optional method return type.
		 */
		private NativeMapper<?> returnMapper(Method method) {
			final Class<?> returnType = method.getReturnType();
			if(returnType == void.class) {
				return null;
			}
			else {
				return context
						.registry()
						.mapper(returnType)
						.orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + method));
			}
		}

		/**
		 * Builds the function descriptor for the given signature.
		 */
		private static FunctionDescriptor descriptor(List<? extends NativeMapper<?>> signature, NativeMapper<?> returnType) {
			final MemoryLayout[] layouts = signature
					.stream()
					.map(NativeMapper::layout)
					.toArray(MemoryLayout[]::new);

			if(returnType == null) {
				return FunctionDescriptor.ofVoid(layouts);
			}
			else {
				return FunctionDescriptor.of(returnType.layout(), layouts);
			}
		}
	}
}
