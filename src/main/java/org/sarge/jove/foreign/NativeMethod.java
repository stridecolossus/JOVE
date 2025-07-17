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
//		private MemoryLayout layout() {
//			if(returned) {
//				return AddressLayout.ADDRESS;
//			}
//			else {
//				return transformer.layout();
//			}
//		}

	private final MethodHandle handle;
	private final Function<Object, ?> returns;
	private final Transformer[] parameters;

	/**
	 * Constructor.
	 * @param handle		Native method
	 * @param returns		Optional return type transformer
	 * @param signature		Parameter transformers
	 * @throws IllegalArgumentException if a {@link #returns} transformer is not configured for a method with a return type
	 * @throws IllegalArgumentException if the number of {@link #parameters} does not match the native method
	 */
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
	 * @return Return value unmarshalling function
	 */
	@SuppressWarnings("unchecked")
	private static Function<Object, ?> returns(Class<?> type, Transformer transformer) {
		if(type == void.class) {
			return Function.identity();
		}
		else {
			return switch(transformer) {
				case null -> throw new IllegalArgumentException("Expected return value transformer");
				case IdentityTransformer _ -> Function.identity();
				case DefaultTransformer<?> def -> (Function<Object, ?>) def.unmarshal();
			};
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
			return returns.apply(result);
		}
		catch(Throwable e) {
			throw new RuntimeException("Error invoking native method: " + this, e);
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
			foreign[n] = TransformerHelper.marshal(args[n], parameters[n], allocator);
		}
		return foreign;
	}

	/**
	 * Unmarshals by-reference arguments.
	 */
	private void update(Object[] args, Object[] foreign) {
		for(int n = 0; n < parameters.length; ++n) {
			// Skip empty elements
			if(Objects.isNull(args[n]) || MemorySegment.NULL.equals(foreign[n])) {
				continue;
			}

			// Overwrite by-reference arguments
			if((parameters[n] instanceof DefaultTransformer def) && def.isReference()) {
				args[n] = unmarshal(foreign[n], def);
			}
		}
	}

	/**
	 * Unmarshals a by-reference parameter.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Object unmarshal(Object value, DefaultTransformer transformer) {
		final Function unmarshal = transformer.unmarshal();
		return unmarshal.apply(value);
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
		return String.format("NativeMethod[%s:%s]", returns, Arrays.toString(parameters));
	}

	/**
	 * Helper - Derives the FFM function descriptor from the given native method signature.
	 * @param returns		Optional return value transformer
	 * @param parameters	Parameter transformers
	 * @returns Function descriptor
	 */
	static FunctionDescriptor descriptor(Transformer returns, List<Transformer> parameters) {
		// Map method signature to FFM layout
		final MemoryLayout[] layouts = parameters
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

//		private static MemoryLayout layout(NativeParameter parameter) {
//			return switch(parameter) {
//				case Transformer<?> transformer -> transformer.layout();
//				case ReferenceParameter _ -> ValueLayout.ADDRESS;
//			};
//		}
