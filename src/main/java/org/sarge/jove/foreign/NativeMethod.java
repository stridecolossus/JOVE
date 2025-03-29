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
	@SuppressWarnings("rawtypes")
	private final Function returns;
	private final MethodHandle handle;
	private final List<? extends NativeTransformer<?>> signature;

	/**
	 * Constructor.
	 * @param handle		Native method
	 * @param returns		Optional return type transformer
	 * @param signature		Parameter transformers
	 * @throws IllegalArgumentException if the {@link #signature} does not match the native method
	 * @throws IllegalArgumentException if {@link #returns} is missing or superfluous
	 * @throws UnsupportedOperationException if the return type is not supported
	 */
	public NativeMethod(MethodHandle handle, NativeTransformer<?> returns, List<? extends NativeTransformer<?>> signature) {
		final MethodType type = handle.type();
		if(signature.size() != type.parameterCount()) {
			throw new IllegalArgumentException("Mismatched number of transforms for method signature");
		}
		if(Objects.isNull(returns) ^ (type.returnType() == void.class)) {
			throw new IllegalArgumentException("Missing or unused returns transformer");
		}

		this.handle = requireNonNull(handle);
		this.returns = validate(returns);
		this.signature = List.copyOf(signature);
	}

	private static Function<? extends Object, ?> validate(NativeTransformer<?> returns) {
		if(returns == null) {
			return null;
		}
		else {
			return returns.unmarshal();
		}
	}

	/**
	 * Invokes this native method with the given arguments.
	 * @param args		Arguments
	 * @param arena		Arena
	 * @return Return value
	 * @throws IllegalArgumentException if an argument or the return value cannot be marshalled
	 * @throws RuntimeException if the native method fails
	 */
	public Object invoke(Object[] args) {
		try {
			@SuppressWarnings("resource")
			final Object[] array = marshal(args, Arena.ofAuto());
			final Object result = handle.invokeWithArguments(array);
			return unmarshal(result);
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

		final Object[] mapped = new Object[args.length];
		for(int n = 0; n < args.length; ++n) {
			mapped[n] = marshal(args[n], signature.get(n), arena);
		}

		return mapped;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Object marshal(Object arg, NativeTransformer transformer, Arena arena) {
		return transformer.marshal(arg, arena);
	}

	/**
	 * Unmarshals the method return value.
	 * @param result Native return value
	 * @return Unmarshalled return value
	 */
	@SuppressWarnings("unchecked")
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
				this.handle.equals(that.handle) &&
				Objects.equals(this.returns, that.returns) &&
				this.signature.equals(that.signature);
	}

	@Override
	public String toString() {
		return String.format("NativeMethod[handle=%s returns=%s signature=%s]", handle, returns, signature);
	}

	/**
	 * The <i>native method factory</i> constructs and links a native method wrapper for a given API method.
	 */
	public static class Factory {
		private final NativeRegistry registry;
		private final Linker linker = Linker.nativeLinker();

		/**
		 * Constructor.
		 * @param registry Native transformers
		 */
		public Factory(NativeRegistry registry) {
			this.registry = registry;
		}

		/**
		 * Builds a native method for the given function pointer and signature.
		 * @param address		Function pointer
		 * @param signature		Method signature
		 * @return Native method
		 * @throws IllegalArgumentException for an unsupported return or type or parameter
		 */
		public NativeMethod build(MemorySegment address, MethodType signature) {
			// Map return type
			final NativeTransformer<?> returns = returns(signature.returnType());

			// Map parameters
			final List<? extends NativeTransformer<?>> parameters = Arrays
					.stream(signature.parameterArray())
					.map(registry::transformer)
					.toList();

			// Build native method handle
			final FunctionDescriptor descriptor = build(returns, parameters);
			final MethodHandle handle = linker.downcallHandle(address, descriptor);

			// Create native method wrapper
			return new NativeMethod(handle, returns, parameters);
		}

		/**
		 * Maps the return transformer for a native method.
		 * @param type Return type
		 * @return Return transformer or {@code null} if none
		 * @throws IllegalArgumentException if the return type is not supported
		 */
		private NativeTransformer<?> returns(Class<?> type) {
			if((type == null) || (type == void.class)) {
				return null;
			}
			else {
				return registry.transformer(type);
			}
		}

		/**
		 * Builds the function descriptor for a native method.
		 * @param returns		Return type
		 * @param signature		Parameter transformers
		 * @return Function descriptor
		 */
		protected static FunctionDescriptor build(NativeTransformer<?> returns, List<? extends NativeTransformer<?>> signature) {
			// Map method signature to FFM layout
			final MemoryLayout[] layouts = signature
					.stream()
					.map(NativeTransformer::layout)
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
