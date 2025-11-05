package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.*;

/**
 * A <i>native method</i> composes an FFM method handle and transformers for its return type and parameters.
 * @see Transformer
 * @author Sarge
 */
public class NativeMethod {
	/**
	 * A <i>native parameter</i> specifies the transformer for a parameter of this native method.
	 */
	@SuppressWarnings("rawtypes")
	public static class NativeParameter {
		private final Transformer transformer;
		private final BiConsumer update;

		/**
		 * Constructor.
		 * @param transformer		Parameter transformer
		 * @param returned			Whether this is a by-reference parameter
		 * @throws UnsupportedOperationException if the given {@link #transformer} cannot be {@link #returned} as a by-reference parameter
		 * @see Returned
		 * @see Transformer#update()
		 */
		public NativeParameter(Transformer transformer, boolean returned) {
			this.transformer = requireNonNull(transformer);

			if(returned) {
				this.update = transformer.update();
			}
			else {
				this.update = null;
			}
		}

		/**
		 * @return Whether this is a by-reference parameter
		 */
		public boolean isReturned() {
			return Objects.nonNull(update);
		}

		/**
		 * Helper.
		 * Overrides the transformer layout as a pointer for by-reference parameters.
		 * @return Memory layout for this parameter
		 */
		MemoryLayout layout() {
			if(isReturned()) {
				return AddressLayout.ADDRESS;
			}
			else {
				return transformer.layout();
			}
		}
	}

	private final MethodHandle handle;
	private final Function<Object, ?> returns;
	private final NativeParameter[] parameters;

	/**
	 * Constructor.
	 * @param handle			Native method
	 * @param returns			Optional return type transformer
	 * @param parameters		Parameter transformers
	 * @throws IllegalArgumentException if a {@link #returns} transformer is not configured for a method with a return type
	 * @throws IllegalArgumentException if the number of {@link #parameters} does not match the native method
	 */
	@SuppressWarnings("rawtypes")
	public NativeMethod(MethodHandle handle, Transformer returns, List<NativeParameter> parameters) {
		final MethodType signature = handle.type();
		if(parameters.size() != signature.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transformers for method signature");
		}

		this.handle = requireNonNull(handle);
		this.returns = returns(signature.returnType(), returns);
		this.parameters = parameters.toArray(NativeParameter[]::new);
	}

	/**
	 * Extracts the unmarshalling function for the return type of this method.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Function<Object, ?> returns(Class<?> type, Transformer returns) {
		if(type == void.class) {
			if(Objects.nonNull(returns)) {
				throw new IllegalArgumentException("Superfluous return transformer");
			}
			return null;
		}
		else {
			if(returns == null) {
				throw new IllegalArgumentException("Expected return transformer");
			}
			return returns.unmarshal();
		}
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
		update(args, foreign);
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
		catch(Throwable e) {
			throw new RuntimeException("Error invoking native method: " + this, e);
		}
	}

	/**
	 * Unmarshals the method return value.
	 */
	private Object unmarshal(Object result) {
		if(returns == null) {
			return null;
		}
		else
		if(MemorySegment.NULL.equals(result)) {
			return null;
		}
		else {
			return returns.apply(result);
		}
	}

	/**
	 * Marshals method arguments to the corresponding FFM types.
	 */
	@SuppressWarnings("resource")
	private Object[] marshal(Object[] args) {
		final var allocator = Arena.ofAuto();
		final Object[] foreign = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			foreign[n] = Transformer.marshal(args[n], parameters[n].transformer, allocator);
		}
		return foreign;
	}

	/**
	 * Unmarshals by-reference arguments.
	 */
	@SuppressWarnings("unchecked")
	private void update(Object[] args, Object[] foreign) {
		for(int n = 0; n < parameters.length; ++n) {
			// Skip empty arguments
			if(args[n] == null) {
				continue;
			}

			// Skip empty arguments
			if(MemorySegment.NULL.equals(foreign[n])) {
				continue;
			}

			// Skip empty arguments
			if(!parameters[n].isReturned()) {
				continue;
			}

			// Overwrite by-reference arguments
			parameters[n].update.accept(foreign[n], args[n]);
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
	 * Helper - Derives the FFM function descriptor from the given native method signature.
	 * @param returns		Optional return value transformer
	 * @param parameters	Parameter transformers
	 * @returns Function descriptor
	 */
	static FunctionDescriptor descriptor(Transformer<?, ?> returns, List<NativeParameter> parameters) {
		// Map method signature to FFM layout
		final MemoryLayout[] layouts = parameters
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
}
