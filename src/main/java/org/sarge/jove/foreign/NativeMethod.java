package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.UnaryOperator;

import org.sarge.jove.foreign.NativeStructure.StructureTransformer;

/**
 * A <i>native method</i> composes a native method and transformers for the return type and parameters.
 * @author Sarge
 */
public class NativeMethod {
	/**
	 * Descriptor for a parameter of this method.
	 */
	record Parameter(Transformer transformer, boolean returned) {
		/**
		 * @return Memory layout of this parameter
		 */
		private MemoryLayout layout() {
			if(returned) {
				return ValueLayout.ADDRESS;
			}
			else {
				return transformer.layout();
			}
		}
		// TODO - all by-reference types must be a pointer (ADDRESS)
	}
	// TODO - make this hidden? => private ctor, can ONLY used builder => but hard to then set the method handle directly in tests

	private static final UnaryOperator<Object> VOID = result -> {
		assert result == null;
		return null;
	};

	private final MethodHandle handle;
	private final UnaryOperator<Object> returns;
	private final Parameter[] signature;

	/**
	 * Constructor.
	 * @param handle		Native method
	 * @param returns		Optional return type transformer
	 * @param signature		Parameter transformers
	 * @throws IllegalArgumentException if the {@link #signature} does not match the native method
	 * @throws IllegalArgumentException if {@link #returns} is missing or superfluous
	 * @throws UnsupportedOperationException if the return type is not supported
	 */
	NativeMethod(MethodHandle handle, UnaryOperator<Object> returns, List<Parameter> signature) {
		final MethodType type = handle.type();
		if(signature.size() != type.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transformers for method signature");
		}
		if((returns == VOID) ^ (type.returnType() == void.class)) {
			throw new IllegalArgumentException("Missing or unused returns transformer");
		}

		this.handle = requireNonNull(handle);
		this.returns = requireNonNull(returns);
		this.signature = signature.toArray(Parameter[]::new);
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
			final Object[] foreign = marshal(args, Arena.ofAuto());
			final Object result = handle.invokeWithArguments(foreign);
			update(foreign, args);
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
	 * @param args			Arguments
	 * @param allocator		Allocator
	 * @return Marshalled arguments
	 */
	private Object[] marshal(Object[] args, SegmentAllocator allocator) {
		if(args == null) {
			return null;
		}

		final Object[] transformed = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			transformed[n] = TransformerHelper.marshal(args[n], signature[n].transformer, allocator);
		}

		return transformed;
	}

	/**
	 * Unmarshals by-reference parameters
	 * @param foreign		Off-heap memory
	 * @param args			Method arguments
	 */
	private void update(Object[] foreign, Object[] args) {
		if(args == null) {
			return;
		}

		for(int n = 0; n < args.length; ++n) {
			if(signature[n].returned && Objects.nonNull(args[n])) {
				// TODO - should 'update' be another type? generic? mixin on transformer => no need for flag!
				switch(signature[n].transformer) {
    	    		case StructureTransformer struct -> struct.unmarshal((MemorySegment) foreign[n], (NativeStructure) args[n]);
    	    		case ArrayTransformer array -> array.update((MemorySegment) foreign[n], (Object[]) args[n]);
    	    		default -> throw new RuntimeException();
    			}
			}
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
		private final List<Parameter> signature = new ArrayList<>();

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
		 * @param returns Return type
		 */
		public Builder returns(Class<?> returns) {
			if((returns == null) || (returns == void.class)) {
				this.returns = null;
			}
			else {
				this.returns = registry.get(returns);
			}
			return this;
		}

		/**
		 * Adds a method parameter.
		 * @param type 			Parameter type
		 * @param returned		Whether this a <i>by reference</i> parameter
		 */
		public Builder parameter(Class<?> type, boolean returned) {
			// TODO - only structures can be updated by-reference using this mechanism (?)
			// ??? arrays of handles, array of structures
			final Transformer transformer = registry.get(type);
			signature.add(new Parameter(transformer, returned));
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
			final UnaryOperator<Object> op = returns(returns);
			return new NativeMethod(handle, op, signature);
		}

		/**
		 * Builds the function descriptor for this native method.
		 * @param returns		Optional return value transformer
		 * @param signature		Parameter transformers
		 * @return Function descriptor
		 */
		protected static FunctionDescriptor descriptor(Transformer returns, List<Parameter> signature) {
			// Map method signature to FFM layout
			final MemoryLayout[] layouts = signature
					.stream()
					.map(Parameter::layout)
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

		private static UnaryOperator<Object> returns(Transformer returns) {
			if(returns == null) {
				return VOID;
			}
			else {
				return result -> TransformerHelper.unmarshal(result, returns);
			}
		}
	}
}
