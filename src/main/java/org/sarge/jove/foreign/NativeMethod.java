package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;

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
public class NativeMethod {
	private final MethodHandle handle;
	private final NativeParameter[] signature;
	private final NativeParameter returns;

	/**
	 * Constructor.
	 * @param handle 			Native method handle
	 * @param signature			Native parameters
	 * @param returnMapper		Optional return type
	 * @throws IllegalArgumentException if the {@link #signature} does not contain the expected number of parameters
	 * @throws IllegalArgumentException if a {@link #returnType} is not provided for a method with a return type or is superfluous
	 */
	private NativeMethod(MethodHandle handle, List<NativeParameter> signature, NativeParameter returns) {
		final MethodType type = handle.type();
		if((type.returnType() == void.class) ^ (returns == null)) {
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
	public Object invoke(Object[] args, NativeContext context) {
		final Object[] actual = marshal(args, context);
		final Object result = execute(actual);
		unmarshal(args, actual);
		return unmarshalReturnValue(result);
	}

	/**
	 * Marshals the given method arguments to the corresponding native representations.
	 * @param args			Arguments
	 * @param context		Native context
	 * @return Marshalled arguments
	 */
	private Object[] marshal(Object[] args, NativeContext context) {
		if(args == null) {
			return null;
		}

		final Object[] mapped = new Object[args.length];
		for(int n = 0; n < mapped.length; ++n) {
			mapped[n] = signature[n].marshal(args[n], context);
		}

		return mapped;
	}

	/**
	 * Executes this native method with the given marshalled arguments.
	 * @param args Marshalled arguments
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

	// TODO
	private void unmarshal(Object[] args, Object[] actual) {
		if(args == null) {
			return;
		}

		for(int n = 0; n < args.length; ++n) {
			final NativeParameter type = signature[n];
			if(type.isReturnedParameter() && Objects.nonNull(args[n])) {
				type.unmarshal((MemorySegment) actual[n], args[n]);
			}
		}
	}

	/**
	 * Unmarshals the return value of this method.
	 * @param value Native return value
	 * @return Unmarshalled return value
	 */
	private Object unmarshalReturnValue(Object value) {
		if(returns == null) {
			assert value == null;
			return null;
		}
		else {
			return returns.returns(value);
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
		private final NativeMapperRegistry registry;

		private MemorySegment address;
		private NativeParameter returns;
		private final List<NativeParameter> signature = new ArrayList<>();

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
		 * @param returned		Whether this is a by-reference parameter
		 * @throws IllegalArgumentException if the type is not supported or cannot be passed by-reference
		 * @see Returned
		 */
		public Builder parameter(Class<?> type, boolean returned) {
			// Lookup native mapper
			final NativeMapper mapper = registry
					.mapper(type)
					.orElseThrow(() -> new IllegalArgumentException("Unsupported parameter type: " + type));

			// Add parameter wrapper
			final var parameter = new NativeParameter(type, mapper, returned);
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
		 */
		public Builder returns(Class<?> type) {

//			if(ArrayReturnValue.class.equals(type)) {
//				final var mapper = new ArrayReturnValueMapper(registry);
//				this.returns = new NativeType(type, mapper, true);
//				return this;
//			}

			// Lookup native mapper
			final NativeMapper mapper = registry
					.mapper(type)
					.orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + type));

			// Create return type wrapper
			this.returns = new NativeParameter(type, mapper, true);

			return this;
		}

		/**
		 * Constructs this native method.
		 * @return Native method
		 * @see NativeMethod#NativeMethod(MethodHandle, List, NativeParameter)
		 */
		public NativeMethod build() {
			final MemoryLayout[] layout = layout();
			final FunctionDescriptor descriptor = descriptor(layout);
			final MethodHandle handle = linker.downcallHandle(address, descriptor);
			return new NativeMethod(handle, signature, returns);
		}

		/**
		 * Builds the memory layout of this method signature.
		 */
		private MemoryLayout[] layout() {
			return signature
					.stream()
					//.map(p -> p.mapper)
					//.map(NativeMapper::layout)
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
	}
}
