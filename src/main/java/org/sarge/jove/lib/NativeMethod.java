package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * A <i>native method</i> abstracts a native method handle.
 * <p>
 * This class composes a <i>method handle</i> for the underlying native method and <i>native mappers</i> for the parameters and the optional return value.
 * <p>
 * The process of invoking a native method is:
 * <ol>
 * <li>marshal each method argument according to the corresponding native mapper of the signature</li>
 * <li>invoke the method handle with the transformed argument list</li>
 * <li>unmarshal the return value as required</li>
 * </ol>
 * <p>
 * The {@link Factory} is used to construct a native method given its address and signature.
 * <p>
 * @author Sarge
 */
public class NativeMethod {
	/**
	 * Descriptor for a parameter of this native method.
	 * @param <T> Parameter type
	 */
	private record NativeParameter<T>(Class<? extends T> type, NativeMapper<? extends T> mapper, boolean returned) {
	}

	/**
	 * Descriptor for the return type of this native method.
	 * @param <T> Return type
	 */
	private record ReturnType<T>(Class<? extends T> type, ReturnMapper<T, ?> mapper) {
	}

	private final MethodHandle handle;
	private final NativeParameter<?>[] signature;
	private final ReturnType<?> returnType;

	/**
	 * Constructor.
	 * @param handle 			Native method handle
	 * @param signature			Native parameters
	 * @param returnMapper		Optional return type
	 * @throws IllegalArgumentException if the {@link #signature} does not contain the expected number of parameters
	 * @throws IllegalArgumentException if a {@link #returnType} is not provided for a method with a return type or is superfluous
	 */
	private NativeMethod(MethodHandle handle, List<NativeParameter<?>> signature, ReturnType<?> returnType) {
		final MethodType type = handle.type();
		if((type.returnType() == void.class) ^ (returnType == null)) {
			throw new IllegalArgumentException("Mismatched or superfluous return type");
		}
		if(type.parameterCount() != signature.size()) {
			throw new IllegalArgumentException("Mismatched method signature");
		}

		this.handle = requireNonNull(handle);
		this.signature = signature.toArray(NativeParameter[]::new);
		this.returnType = returnType;
	}

	/**
	 * Invokes this native method with the given arguments.
	 * @param args 			Arguments
	 * @param context		Native context
	 * @return Return value or {@code null} for a {@code void} method
	 * @throws RuntimeException if the native method fails
	 */
	public Object invoke(Object[] args, NativeContext context) {
		final Object[] actual = marshal(args, context);
		final Object result = execute(actual);
		return marshalReturnValue(result);
	}

	/**
	 * Maps arguments to the corresponding native representations.
	 * @param args			Arguments
	 * @param context		Native context
	 * @return Mapped arguments
	 */
	private Object[] marshal(Object[] args, NativeContext context) {
		if(args == null) {
			return null;
		}

		final Object[] mapped = new Object[args.length];
		for(int n = 0; n < mapped.length; ++n) {
			mapped[n] = context.toNative(signature[n].mapper, args[n], signature[n].type);
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
	 * Marshals the return value of this method.
	 */
	private Object marshalReturnValue(Object value) {
		if(returnType == null) {
			assert value == null;
			return null;
		}
		else
		if(MemorySegment.NULL.equals(value)) {
			return null;
		}
		else {
			return marshalReturnValue(value, returnType);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Object marshalReturnValue(Object value, ReturnType returnType) {
		return returnType.mapper.fromNative(value, returnType.type);
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeMethod that) &&
				this.handle.equals(that.handle);
	}

	@Override
	public String toString() {
		return String.format("NativeMethod[%s]", handle);
	}

	/**
	 * Builder for a native method.
	 */
	public static class Builder {
		private final Linker linker = Linker.nativeLinker();
		private final NativeMapperRegistry registry;

		private MemorySegment address;
		private ReturnType<?> returnType;
		private final List<NativeParameter<?>> signature = new ArrayList<>();

		/**
		 * Constructor.
		 * @param registry Native mappers
		 */
		public Builder(NativeMapperRegistry registry) {
			this.registry = requireNonNull(registry);
		}

		/**
		 * Sets the memory address of this method.
		 * @param address Method memory address
		 */
		public Builder address(MemorySegment address) {
			this.address = address;
			return this;
		}

		/**
		 * Adds a parameter of the given type.
		 * @param type Parameter type
		 * @throws IllegalArgumentException if the type is not supported
		 */
		public Builder parameter(Class<?> type) {
			return parameter(type, false);
		}

		/**
		 * Adds a parameter of the given type.
		 * @param type 			Parameter type
		 * @param returned		Whether this is a <i>returned</i> parameter
		 * @throws IllegalArgumentException if the type is not supported or cannot be a <i>returned</i> type
		 */
		public Builder parameter(Class<?> type, boolean returned) {
			// Lookup native mapper
			final NativeMapper<?> mapper = registry
					.mapper(type)
					.orElseThrow(() -> new IllegalArgumentException("Unsupported parameter type: " + type));

			// Check can be a returned parameter if required
			if(returned && !(mapper instanceof ReturnMapper)) {
				throw new IllegalArgumentException("Parameter cannot be returned: " + type);
			}

			// Add parameter wrapper
			final var parameter = new NativeParameter<>(type, mapper, returned);
			signature.add(parameter);

			return this;
		}

		/**
		 * Helper - Sets the method signature.
		 * @param signature Parameter types
		 */
		public Builder signature(Class<?>... signature) {
			for(var type : signature) {
				parameter(type);
			}
			return this;
		}

		/**
		 * Sets the return type of this method.
		 * @param type Return type
		 * @throws IllegalArgumentException if the type is not supported or cannot be returned from this method
		 */
		public Builder returns(Class<?> type) {
			// Lookup native mapper
			final NativeMapper<?> mapper = registry
					.mapper(type)
					.orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + type));

			// Ensure can be returned
			if(!(mapper instanceof ReturnMapper returnMapper)) {
				throw new IllegalArgumentException("Cannot be returned from a native method: " + type);
			}

			// Set return type wrapper
			@SuppressWarnings("unchecked")
			final var rt = new ReturnType<>(type, returnMapper);
			this.returnType = rt;

			return this;
		}

		/**
		 * Constructs this native method.
		 * @return Native method
		 * @see NativeMethod#NativeMethod(MethodHandle, List, ReturnType)
		 */
		public NativeMethod build() {
			final MemoryLayout[] layout = layout();
			final FunctionDescriptor descriptor = descriptor(layout);
			final MethodHandle handle = linker.downcallHandle(address, descriptor);
			return new NativeMethod(handle, signature, returnType);
		}

		/**
		 * Builds the memory layout of this method signature.
		 */
		private MemoryLayout[] layout() {
			return signature
					.stream()
					.map(p -> p.mapper)
					.map(NativeMapper::layout)
					.toArray(MemoryLayout[]::new);
		}

		/**
		 * Builds the function descriptor of this method.
		 */
		private FunctionDescriptor descriptor(MemoryLayout[] layout) {
			if(returnType == null) {
				return FunctionDescriptor.ofVoid(layout);
			}
			else {
				final MemoryLayout m = returnType.mapper.layout();
				return FunctionDescriptor.of(m, layout);
			}
		}
	}
}
