package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Function;

/**
 * A <i>native method</i> composes an FFM method handle and transformers for its return type and parameters.
 * @see Transformer
 * @author Sarge
 */
public class NativeMethod {
	/**
	 * A <i>native parameter</i> is a descriptor for a parameter of this native method.
	 */
	record NativeParameter(Transformer<?> transformer, boolean returned) {
		/**
		 * Constructor.
		 * @param transformer		Argument transformer
		 * @param returned			Whether this parameter is returned-by-reference
		 */
		public NativeParameter {
			requireNonNull(transformer);
		}

		/**
		 * Convenience constructor.
		 * @param transformer Argument transformer
		 */
		public NativeParameter(Transformer<?> transformer) {
			this(transformer, false);
		}

		/**
		 * @return Memory layout of this parameter
		 */
		private MemoryLayout layout() {
			if(returned) {
				return AddressLayout.ADDRESS;
			}
			else {
				return transformer.layout();
			}
		}
	}

	private final MethodHandle handle;
	private final Function<Object, ?> returns;
	private final List<NativeParameter> signature;

	/**
	 * Constructor.
	 * @param handle		Native method
	 * @param returns		Optional return type transformer
	 * @param signature		Parameters
	 * @throws IllegalArgumentException if the {@link #signature} does not match the native method
	 * @throws IllegalArgumentException if the {@link #returns} transformer is missing or superfluous
	 */
	NativeMethod(MethodHandle handle, Function<Object, ?> returns, List<NativeParameter> signature) {
		final MethodType type = handle.type();
		if(signature.size() != type.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transformers for method signature");
		}
		if((returns == null) ^ (type.returnType() == void.class)) {
			throw new IllegalArgumentException("Missing or unused returns transformer");
		}

		this.handle = requireNonNull(handle);
		this.returns = returns;
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
		if(args == null) {
			return invokeLocal(null);
		}

		final Object[] foreign = marshal(args);
		final Object result = invokeLocal(foreign);
		update(args);
		return result;
	}

	/**
	 * Invokes this native method and unmarshals the return value.
	 * @param args Marshalled arguments
	 * @return Unmarshalled return value
	 */
	private Object invokeLocal(Object[] args) {
		try {
			final Object result = handle.invokeWithArguments(args);
			return unmarshal(result);
		}
		catch(IllegalArgumentException e) {
			throw e;
		}
		catch(Throwable e) {
			throw new RuntimeException("Error invoking native method: " + this, e);
		}
	}

	/**
	 * Marshals the given arguments to the corresponding FFM types.
	 */
	@SuppressWarnings("resource")
	private Object[] marshal(Object[] args) {
		// TODO - make helper an instance with arena
		final var allocator = Arena.ofAuto();
		final Object[] transformed = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			final Transformer<?> transformer = signature.get(n).transformer();
			transformed[n] = Transformer.marshal(args[n], transformer, allocator);
		}
		return transformed;
	}

	/**
	 * Unmarshals by-reference arguments.
	 */
	private static void update(Object[] args) {
		for(int n = 0; n < args.length; ++n) {

			// native reference
			// handle array
			// structure array
			// structure?

//				switch(signature[n].transformer) {
//					// TODO - urgh
//					case StructureTransformer structure -> structure.update((MemorySegment) foreign[n], (NativeStructure) args[n]);
//					case ArrayTransformer array -> array.update((MemorySegment) foreign[n], (Object[]) args[n]);
//					default -> throw new RuntimeException();
//				}
		}
	}

	/**
	 * Unmarshals the return value.
	 */
	private Object unmarshal(Object result) {
		if(returns == null) {
			assert result == null;
			return null;
		}
		else {
			return returns.apply(result);
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
		return String.format("NativeMethod[handle=%s returns=%s signature=%s]", handle, returns, signature);
	}

	/**
	 * Builder for a native method.
	 */
	public static class Builder {
		private final Registry registry;
		private final Linker linker;

		private MemorySegment address;
		private Transformer<?> returns;
		private List<NativeParameter> signature = new ArrayList<>();

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
		 * @param type Method return type
		 * @throws IllegalArgumentException if the return type is unsupported
		 */
		public Builder returns(Class<?> type) {
			// Ignore methods without a return value
			if(type == void.class) {
				this.returns = null;
				return this;
			}

			// Lookup return type transformer
			this.returns = registry.transformer(type);

			return this;
		}

		/**
		 * Adds a parameter to the signature of this method.
		 * @param type Parameter type
		 * @throws IllegalArgumentException if the parameter type is unsupported
		 * @see #parameter(Class, boolean)
		 */
		public Builder parameter(Class<?> type) {
			return parameter(type, false);
		}

		/**
		 * Adds a parameter to the signature of this method.
		 * @param type 			Parameter type
		 * @param returned		Whether this parameter is returned-by-reference
		 * @throws IllegalArgumentException if the parameter type is unsupported
		 */
		public Builder parameter(Class<?> type, boolean returned) {
			final Transformer<?> transformer = registry.transformer(type);
			signature.add(new NativeParameter(transformer, returned));
			// TODO - check can be by-reference?
			return this;
		}

		/**
		 * Constructs this native method.
		 * @return Native method
		 * @throws UnsupportedOperationException if the return type is unsupported
		 */
		public NativeMethod build() {
			final FunctionDescriptor descriptor = descriptor();
			final MethodHandle handle = linker.downcallHandle(descriptor).bindTo(address);
			return new NativeMethod(handle, returns(), signature);
		}

		/**
		 * Builds the function descriptor for this native method.
		 */
		private FunctionDescriptor descriptor() {
			// Map method signature to FFM layout
			final MemoryLayout[] layouts = signature
					.stream()
					.map(NativeParameter::layout)
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

		/**
		 * Looks up the return value transformer for this method.
		 */
		@SuppressWarnings("unchecked")
		private Function<Object, ?> returns() {
			if(returns == null) {
				return null;
			}
			else {
				return (Function<Object, ?>) returns.unmarshal();
			}
		}
	}
}
