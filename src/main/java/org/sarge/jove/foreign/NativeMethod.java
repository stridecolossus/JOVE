package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Function;

/**
 * A <i>native method</i> abstracts a native method handle.
 * <p>
 * This class composes a <i>method handle</i> for the underlying native method and <i>native mappers</i> for the parameters and the optional return value.
 * <p>
 * The process of invoking a native method is:
 * <ol>
 * <li>marshal each method argument according to the corresponding native mapper of the signature</li>
 * <li>invoke the method handle with the transformed argument list</li>
 * <li>unmarshal any pass-by-reference parameters</li>
 * <li>unmarshal the return value if present</li>
 * </ol>
 * <p>
 * @see Returned
 * <p>
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class NativeMethod {
	/**
	 * Native parameter mapper optionally for a by-reference parameter.
	 */
	private record NativeParameter(NativeTransformer transformer, boolean returned) {
		/**
		 * @return Memory layout of this parameter
		 */
		MemoryLayout layout() {
			return transformer.layout();
		}

		/**
		 * Transforms a method argument.
		 * @param arg			Argument
		 * @param allocator		Allocator
		 * @return Marshalled argument
		 */
		Object transform(Object arg, SegmentAllocator allocator) {
			return NativeTransformer.transform(arg, transformer, allocator);
		}

		/**
		 * Updates a by-reference argument.
		 * @param value		Native value
		 * @param arg		By-reference argument
		 */
		@SuppressWarnings("unchecked")
		void update(Object value, Object arg) {
			assert returned;
			transformer.update().accept(value, arg);
		}
	}

	private final MethodHandle handle;
	private final NativeParameter[] signature;
	private final Function returns;

	/**
	 * Constructor.
	 * @param handle 			Native method handle
	 * @param signature			Native parameters
	 * @param returnMapper		Optional return value mapper
	 * @throws IllegalArgumentException if the {@link #signature} does not contain the expected number of parameters
	 * @throws IllegalArgumentException if a {@link #returns} is not provided for a method with a return type or is superfluous
	 */
	private NativeMethod(MethodHandle handle, List<NativeParameter> signature, Function returns) {
		final MethodType type = handle.type();
		if((type.returnType() == void.class) ^ Objects.isNull(returns)) {
			throw new IllegalArgumentException("Mismatched or superfluous return type");
		}
		if(type.parameterCount() != signature.size()) {
			throw new IllegalArgumentException("Mismatched method signature");
		}

		this.handle = requireNonNull(handle);
		this.signature = signature.toArray(NativeParameter[]::new);
		this.returns = returns;
	}

	/**
	 * Invokes this native method with the given arguments.
	 * @param args 			Arguments
	 * @param context		Native context
	 * @return Return value or {@code null} for a {@code void} method
	 * @throws RuntimeException if the native method fails
	 */
	public Object invoke(Object[] args, SegmentAllocator allocator) {
		final Object[] actual = transform(args, allocator);
		final Object result = execute(actual);
		update(args, actual);
		return transform(result);
	}

	/**
	 * Transform the given method arguments to the corresponding native representation.
	 * @param args			Arguments
	 * @param context		Native context
	 * @return Transformed arguments
	 */
	private Object[] transform(Object[] args, SegmentAllocator allocator) {
		if(args == null) {
			return null;
		}

		final Object[] transformed = new Object[args.length];
		for(int n = 0; n < transformed.length; ++n) {
			final NativeParameter p = signature[n];
			transformed[n] = p.transform(args[n], allocator);
		}

		return transformed;
	}

	/**
	 * Executes this native method with the given transformed arguments.
	 * @param args Transformed arguments
	 * @return Return value
	 */
	private Object execute(Object[] args) {
		try {
			// TODO - invoke?
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
			final NativeParameter p = signature[n];
			final Object arg = args[n];
			if(p.returned && Objects.nonNull(arg)) {
				p.update(actual[n], arg);
			}
		}
	}

	/**
	 * Transforms the return value of this method.
	 * @param value Native return value
	 * @return Transformed return value
	 */
	@SuppressWarnings("unchecked")
	private Object transform(Object value) {
		if(returns == null) {
			assert value == null;
			return null;
		}
		else {
			return returns.apply(value);
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
	 * Builder for a native method.
	 */
	public static class Builder {
		private final Linker linker = Linker.nativeLinker();
		private final TransformerRegistry registry;

		private MemorySegment address;
		private final List<NativeParameter> signature = new ArrayList<>();
		private NativeTransformer returns;

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
			return parameter(type, false);
		}

		/**
		 * Adds a parameter of the given type.
		 * @param type 			Parameter type
		 * @param returned		Whether this is a by-reference parameter
		 * @throws IllegalArgumentException if the type is not supported or cannot be passed by-reference
		 * @see Returned
		 */
		public Builder parameter(Class<?> type, boolean returned) {
			final NativeTransformer mapper = registry.get(type);
			signature.add(new NativeParameter(mapper, returned));
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
		 */
		public Builder returns(Class<?> type) {
			this.returns = registry.get(type);
			return this;
		}

		/**
		 * Constructs this native method.
		 * @return Native method
		 * @see NativeMethod#NativeMethod(MethodHandle, List, Function)
		 */
		public NativeMethod build() {
			final MemoryLayout[] layout = layout();
			final FunctionDescriptor descriptor = descriptor(layout);
			final MethodHandle handle = linker.downcallHandle(address, descriptor);
			return new NativeMethod(handle, signature, returns());
		}

		/**
		 * Builds the memory layout of this method signature.
		 */
		private MemoryLayout[] layout() {
			return signature
					.stream()
					.map(NativeParameter::layout)
					.toArray(MemoryLayout[]::new);
		}

		/**
		 * Builds the function descriptor of this method.
		 */
		private FunctionDescriptor descriptor(MemoryLayout[] layout) {
			if(returns == null) {
				return FunctionDescriptor.ofVoid(layout);
			}
			else {
				final MemoryLayout m = returns.layout();
				return FunctionDescriptor.of(m, layout);
			}
		}

		/**
		 * @return Return value transformer
		 */
		private Function returns() {
			if(returns == null) {
				return null;
			}
			else {
				return returns.returns();
			}
		}
	}
}
