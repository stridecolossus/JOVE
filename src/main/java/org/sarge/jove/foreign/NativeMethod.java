package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Function;

/**
 * A <i>native method</i> composes a native method and transformers for the return type and parameters.
 * @author Sarge
 */
public class NativeMethod {
	private final Function<Object, Object> returns;
	private final MethodHandle handle;
	private final List<Transformer> signature;

	/**
	 * Constructor.
	 * @param handle		Native method
	 * @param returns		Optional return type transformer
	 * @param signature		Parameter transformers
	 * @throws IllegalArgumentException if the {@link #signature} does not match the native method
	 * @throws IllegalArgumentException if {@link #returns} is missing or superfluous
	 * @throws UnsupportedOperationException if the return type is not supported
	 */
	public NativeMethod(MethodHandle handle, Transformer returns, List<Transformer> signature) {
		final MethodType type = handle.type();
		if(signature.size() != type.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transformers for method signature");
		}
		if(Objects.isNull(returns) ^ (type.returnType() == void.class)) {
			throw new IllegalArgumentException("Missing or unused returns transformer");
		}

		this.handle = requireNonNull(handle);
		this.returns = TransformerHelper.unmarshal(returns);
		this.signature = List.copyOf(signature);
	}

	/**
	 * Invokes this native method with the given arguments.
	 * @param args Arguments
	 * @return Return value
	 * @throws IllegalArgumentException if an argument or the return value cannot be marshalled
	 * @throws RuntimeException if the native method fails
	 */
	public Object invoke(Object[] args) {
		try {
			@SuppressWarnings("resource")
			final Object[] transformed = marshal(args, Arena.ofAuto());
			final Object result = handle.invokeWithArguments(transformed);
			return returns.apply(result);
		}
		catch(IllegalArgumentException e) {
			throw e;
		}
		catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Marshals the given arguments to the corresponding FFM types.
	 * @param args		Arguments
	 * @param arena		Arena
	 * @return Marshalled arguments
	 */
	private Object[] marshal(Object[] args, Arena arena) {
		if(args == null) {
			return null;
		}

		final Object[] transformed = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			transformed[n] = TransformerHelper.marshal(args[n], signature.get(n), arena);
		}

		return transformed;
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
				this.handle.equals(that.handle) &&
				Objects.equals(this.returns, that.returns) &&
				this.signature.equals(that.signature);
	}

	@Override
	public String toString() {
		return String.format("NativeMethod[handle=%s returns=%s signature=%s]", handle, returns, signature);
	}

	/**
	 * Builder for a native method.
	 */
	public static class Builder {
		private final Registry registry;
		private final Linker linker;

		private MemorySegment address;
		private Transformer returns;
		private final List<Transformer> signature = new ArrayList<>();

		/**
		 * Constructor.
		 * @param registry		Transformer registry
		 * @param linker		Native linker
		 */
		public Builder(Registry registry, Linker linker) {
			this.registry = requireNonNull(registry);
			this.linker = requireNonNull(linker);
		}

		/**
		 * Constructor.
		 * @param registry Native transformers
		 */
		public Builder(Registry registry) {
			this(registry, Linker.nativeLinker());
		}

		/**
		 * Sets the function pointer of this method.
		 * @param address Function pointer
		 */
		public Builder address(MemorySegment address) {
			this.address = requireNonNull(address);
			return this;
		}

		/**
		 * Sets the return type of this method.
		 * @param type Return type
		 */
		public Builder returns(Class<?> type) {
			if((type == null) || (type == void.class)) {
				this.returns = null;
			}
			else {
				this.returns = registry.get(type);
			}
			return this;
		}

		/**
		 * Adds a method parameter.
		 * @param type 			Parameter type
		 * @param returned		Whether this a <i>by reference</i> parameter
		 */
		public Builder parameter(Class<?> type, boolean returned) {
			// TODO - returned
			final Transformer transformer = registry.get(type);
			signature.add(transformer);
			return this;
		}

		/**
		 * Adds a method parameter.
		 * @param type Parameter type
		 */
		public Builder parameter(Class<?> type) {
			return parameter(type, false);
		}

		/**
		 * Constructs this native method.
		 * @return Native method
		 */
		public NativeMethod build() {
			final FunctionDescriptor descriptor = descriptor(returns, signature);
			final MethodHandle handle = linker.downcallHandle(descriptor).bindTo(address);
			return new NativeMethod(handle, returns, signature);
		}

		/**
		 * Builds the function descriptor for this native method.
		 * @param returns		Optional return value transformer
		 * @param signature		Parameter transformers
		 * @return Function descriptor
		 */
		protected static FunctionDescriptor descriptor(Transformer returns, List<Transformer> signature) {
			// Map method signature to FFM layout
			final MemoryLayout[] layouts = signature
					.stream()
					.map(Transformer::layout)
					.toArray(MemoryLayout[]::new);

			// Init descriptor
			final FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(layouts);

			// Append return layout
			if(returns == null) {
				return descriptor;
			}
			else {
				return descriptor.changeReturnLayout(returns.layout());
			}
		}
	}
}
