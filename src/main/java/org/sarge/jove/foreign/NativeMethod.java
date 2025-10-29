package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;

import org.sarge.jove.foreign.Transformer.ReturnedTransformer;

/**
 * A <i>native method</i> composes an FFM method handle and transformers for its return type and parameters.
 * @see Transformer
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class NativeMethod {
	/**
	 * A <i>native parameter</i> specifies the transformer for a parameter of this native method.
	 */
	public record NativeParameter(Transformer transformer, ReturnedTransformer update) {
		/**
		 * Constructor.
		 * @param transformer		Parameter transformer
		 * @param update			Optional transformer for a by-reference parameter
		 * @see Returned
		 */
		public NativeParameter {
			requireNonNull(transformer);
		}
	}

	private final MethodHandle handle;
	private final Transformer returns;
	private final NativeParameter[] parameters;

	/**
	 * Constructor.
	 * @param handle				Native method
	 * @param returns				Optional return type transformer
	 * @param signature				Parameter transformers
	 * @throws IllegalArgumentException if a {@link #returns} transformer is not configured for a method with a return type
	 * @throws IllegalArgumentException if the number of {@link #parameters} does not match the native method
	 */
	public NativeMethod(MethodHandle handle, Transformer returns, List<NativeParameter> parameters) {
		final MethodType signature = handle.type();
		if(parameters.size() != signature.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transformers for method signature");
		}
		if(Objects.isNull(returns) ^ (handle.type().returnType() == void.class)) {
			throw new IllegalArgumentException("Missing or superfluous return transformer");
		}

		this.handle = requireNonNull(handle);
		this.returns = returns;
		this.parameters = parameters.toArray(NativeParameter[]::new);
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
	@SuppressWarnings("unchecked")
	private Object unmarshal(Object result) {
		if(returns == null) {
			return null;
		}
		else
		if(MemorySegment.NULL.equals(result)) {
			return null;
		}
		else {
			return returns.unmarshal(result);
		}
	}

	/**
	 * Marshals method arguments to the corresponding FFM types.
	 */
	@SuppressWarnings({"resource", "unchecked"})
	private Object[] marshal(Object[] args) {
		final var allocator = Arena.ofAuto();
		final Object[] foreign = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			if(args[n] == null) {
				foreign[n] = MemorySegment.NULL;
			}
			else {
				foreign[n] = parameters[n].transformer.marshal(args[n], allocator);
			}
		}
		return foreign;
	}

	/**
	 * Unmarshals by-reference arguments.
	 */
	private void update(Object[] args, Object[] foreign) {
		for(int n = 0; n < parameters.length; ++n) {
			// Skip empty parameters
			if(args[n] == null) {
				continue;
			}

			// Skip read-only parameters
			if(parameters[n].update == null) {
				continue;
			}

			// Overwrite by-reference arguments
			final ReturnedTransformer update = parameters[n].update;
			if(MemorySegment.NULL.equals(foreign[n])) {
				update.update(null, args[n]);		// TODO - or ignore?
			}
			else {
				update.update((MemorySegment) foreign[n], args[n]);		// TODO - cast
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
				this.handle.equals(that.handle);
	}

	@Override
	public String toString() {
		return String.format("NativeMethod[%s]", handle);
	}

	/**
	 * Helper factory used to create a native method.
	 */
	public static class Factory {
		private final Linker linker = Linker.nativeLinker();

		/**
		 * Creates a native method.
		 * @param address			Method address
		 * @param returns			Optional return value transformer
		 * @param parameters		Parameter transformers
		 * @return Native method
		 * @throws RuntimeException if the method cannot be linked to the native library
		 */
		public NativeMethod create(MemorySegment address, Transformer returns, List<NativeParameter> parameters) {
			final FunctionDescriptor descriptor = descriptor(returns, parameters);
			final MethodHandle handle = linker.downcallHandle(descriptor).bindTo(address);
			return new NativeMethod(handle, returns, parameters);
		}

    	/**
    	 * Helper - Derives the FFM function descriptor from the given native method signature.
    	 * @param returns		Optional return value transformer
    	 * @param parameters	Parameter transformers
    	 * @returns Function descriptor
    	 */
    	private static FunctionDescriptor descriptor(Transformer returns, List<NativeParameter> parameters) {
    		// Map method signature to FFM layout
    		final MemoryLayout[] layouts = parameters
    				.stream()
    				.map(p -> p.transformer.layout())
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
