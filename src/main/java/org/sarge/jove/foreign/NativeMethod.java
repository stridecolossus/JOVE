package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Function;

import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

/**
 * A <i>native method</i> abstracts over a native <i>method handle</i> and applies <i>transformers</i> to marshal Java or JOVE domain types to/from the FFM equivalents.
 * <p>
 * The process of invoking a native method is:
 * <ol>
 * <li>convert each argument according to the associated transformer</li>
 * <li>invoke the underlying native method handle with the transformed argument list</li>
 * <li>update any by-reference parameters</li>
 * <li>transform the return value (if present)</li>
 * </ol>
 * <p>
 * A native method is configured and constructed by the {@link Builder}.
 * <p>
 * @see NativeTransformer
 * @see Returned
 * <p>
 * @author Sarge
 */
public class NativeMethod {
	private final MethodHandle handle;
	private final NativeParameter[] signature;
	private final ReturnType returns;

	/**
	 * Constructor.
	 * @param handle 			Native method handle
	 * @param signature			Native parameters
	 * @param returnMapper		Optional return value mapper
	 * @throws IllegalArgumentException if the {@link #signature} does not contain the expected number of parameters
	 * @throws IllegalArgumentException if a {@link #returns} is not provided for a method with a return type or is superfluous
	 */
	private NativeMethod(MethodHandle handle, List<NativeParameter> signature, ReturnType returns) {
		final MethodType type = handle.type();
		if(type.parameterCount() != signature.size()) {
			throw new IllegalArgumentException("Mismatched method signature");
		}
		if((type.returnType() == void.class) ^ Objects.isNull(returns)) {
			throw new IllegalArgumentException("Mismatched or superfluous return type");
		}
		if(Objects.nonNull(returns) && type.returnType().isArray()) {
			throw new IllegalArgumentException("Arrays cannot be returned from a native method");
		}

		this.handle = requireNonNull(handle);
		this.signature = signature.toArray(NativeParameter[]::new);
		this.returns = returns;
	}

	/**
	 * Invokes this native method with the given arguments.
	 * @param args 			Arguments
	 * @param allocator		Allocator
	 * @return Return value or {@code null} for a {@code void} method
	 * @throws IllegalArgumentException if the number of arguments does not match the signature of this method
	 * @throws RuntimeException if the native method fails
	 * @see NativeTransformer#transform(Object, ParameterType, SegmentAllocator)
	 */
	public Object invoke(Object[] args, SegmentAllocator allocator) {
		final Object[] actual = transform(args, allocator);
		final Object result = execute(actual);
		update(args, actual);
		return returns(result);
	}

	/**
	 * Transforms the given method arguments to the corresponding native representation.
	 * @param args			Arguments
	 * @param context		Native context
	 * @return Transformed arguments
	 */
	private Object[] transform(Object[] args, SegmentAllocator allocator) {
		if(args == null) {
			validate(0);
			return null;
		}
		else {
			validate(args.length);
		}

		final Object[] transformed = new Object[args.length];
		for(int n = 0; n < transformed.length; ++n) {
			final var p = signature[n];
			transformed[n] = p.transform(args[n], allocator);
		}

		return transformed;
	}

	private void validate(int count) {
		if(count != signature.length) {
			throw new IllegalArgumentException("Invalid number of arguments for method %s, expected=%d actual=%d".formatted(handle, signature.length, count));
		}
	}

	/**
	 * Executes this native method with the given transformed arguments.
	 * @param args Transformed arguments
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
	 * Updates by-reference parameters after invocation.
	 * @param args		Method arguments
	 * @param actual	Transformed arguments
	 */
	private void update(Object[] args, Object[] actual) {
		if(args == null) {
			return;
		}

		for(int n = 0; n < args.length; ++n) {
			final Object arg = args[n];
			final var p = signature[n];
			if((p.mode == ParameterMode.REFERENCE) && Objects.nonNull(arg)) {
				p.update((MemorySegment) actual[n], arg);
			}
		}
	}

	/**
	 * Transforms the return value of this method.
	 * @param value Native return value
	 * @return Transformed return value
	 */
	@SuppressWarnings("unchecked")
	private Object returns(Object value) {
		if(returns == null) {
			assert value == null;
			return null;
		}
		else {
			return returns.function.apply(value);
		}
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
	 * Descriptor for a parameter of this method.
	 */
	@SuppressWarnings("rawtypes")
	private record NativeParameter(NativeTransformer transformer, ParameterMode mode) {
		/**
		 * @return Memory layout of this parameter
		 */
		public MemoryLayout layout() {
			if(mode == ParameterMode.REFERENCE) {
				return ValueLayout.ADDRESS;
			}
			else {
				return transformer.layout();
			}
		}

		/**
		 * Transforms this parameter.
		 * @param arg			Argument
		 * @param allocator		Allocator
		 * @return Transformed argument
		 */
		@SuppressWarnings("unchecked")
		public Object transform(Object arg, SegmentAllocator allocator) {
			return transformer.transform(arg, mode, allocator);
		}

		/**
		 * Updates this by-reference parameter.
		 * @param address		Off-heap memory
		 * @param arg			Argument to update
		 */
		@SuppressWarnings("unchecked")
		public void update(MemorySegment address, Object arg) {
			// TODO - cache?
			transformer.update().accept(address, arg);
		}
	}

	/**
	 * Descriptor for the return type of this method.
	 */
	@SuppressWarnings("rawtypes")
	private record ReturnType(MemoryLayout layout, Function function) {
	}

	/**
	 * Builder for a native method.
	 */
	public static class Builder {
		private final Linker linker = Linker.nativeLinker();
		private final TransformerRegistry registry;

		private MemorySegment address;
		private final List<NativeParameter> signature = new ArrayList<>();
		private ReturnType returns;

		/**
		 * Constructor.
		 * @param registry Native transformers
		 */
		public Builder(TransformerRegistry registry) {
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
			return parameter(type, ParameterMode.VALUE);
		}

		/**
		 * Adds a parameter of the given type.
		 * @param type 				Parameter type
		 * @param mode		Parameter mode
		 * @throws IllegalArgumentException if the type is not supported or cannot be passed by-reference
		 * @see Returned
		 */
		public Builder parameter(Class<?> type, ParameterMode mode) {
			requireNonNull(mode);
			final var transformer = registry.get(type);
			final var parameter = new NativeParameter(transformer, mode);
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
		 * @throws IllegalArgumentException if the type is not supported or cannot be returned from a native method
		 * @see ArrayReturnValue
		 */
		public Builder returns(Class<?> type) {
			// Check for special case of a returned array
			if(ArrayReturnValue.class.isAssignableFrom(type)) {
				final Function<MemorySegment, ArrayReturnValue<?>> mapper = address -> new ArrayReturnValue<>(address, registry);
				this.returns = new ReturnType(ADDRESS, mapper);
				return this;
			}

			// Create return type descriptor from transformer
			final var transformer = registry.get(type);
			final MemoryLayout layout = transformer.layout(); // type);
			final var function = transformer.returns(); // type);
			this.returns = new ReturnType(layout, function);

			return this;
		}

		/**
		 * Constructs this native method.
		 * @return Native method
		 */
		public NativeMethod build() {
			final FunctionDescriptor descriptor = descriptor();
			final MethodHandle handle = linker.downcallHandle(address, descriptor);
			return new NativeMethod(handle, signature, returns);
		}

		/**
		 * Builds the function descriptor of this method.
		 */
		private FunctionDescriptor descriptor() {
			final MemoryLayout[] layout = signature
					.stream()
					.map(NativeParameter::layout)
					.toArray(MemoryLayout[]::new);

			if(returns == null) {
				return FunctionDescriptor.ofVoid(layout);
			}
			else {
				return FunctionDescriptor.of(returns.layout, layout);
			}
		}
	}
}
