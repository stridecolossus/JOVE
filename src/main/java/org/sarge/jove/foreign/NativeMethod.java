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
	private final MethodHandle handle;
	private final Function<Object, ?> returns;
	@SuppressWarnings("rawtypes")
	private final Transformer[] parameters;

	/**
	 * Constructor.
	 * @param handle			Native method
	 * @param returns			Optional return type transformer
	 * @param parameters		Parameter transformers
	 * @throws IllegalArgumentException if a {@link #returns} transformer is not configured for a method with a return type
	 * @throws IllegalArgumentException if the number of {@link #parameters} does not match the native method
	 */
	@SuppressWarnings("rawtypes")
	public NativeMethod(MethodHandle handle, Transformer returns, List<Transformer> parameters) {
		final MethodType signature = handle.type();
		if(parameters.size() != signature.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transformers for method signature");
		}

		this.handle = requireNonNull(handle);
		this.returns = returns(signature.returnType(), returns);
		this.parameters = parameters.toArray(Transformer[]::new);
	}

	/**
	 * Extracts the unmarshalling function for the return type of this method.
	 * @param type			Return type
	 * @param returns		Return transformer
	 * @return Return value unmarshalling function
	 * @throws IllegalArgumentError if the function is missing or superfluous
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
	 * @param arg Domain arguments
	 * @return Transformed arguments
	 */
	@SuppressWarnings("resource")
	private Object[] marshal(Object[] args) {
		final var allocator = Arena.ofAuto();
		final Object[] foreign = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			foreign[n] = Transformer.marshal(args[n], parameters[n], allocator);
		}
		return foreign;
	}

	/**
	 * Unmarshals by-reference arguments.
	 * @param args			Domain arguments
	 * @param foreign		Off-heap arguments
	 */
	@SuppressWarnings("unchecked")
	private void update(Object[] args, Object[] foreign) {
		for(int n = 0; n < parameters.length; ++n) {
			// Skip empty arguments
			if(args[n] == null) {
				continue;
			}

			// Skip empty off-heap arguments
			if(MemorySegment.NULL.equals(foreign[n])) {
				continue;
			}

			// Overwrite by-reference arguments
			if(parameters[n] instanceof UpdateTransformer) {
				parameters[n].update().accept(foreign[n], args[n]);
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
}
