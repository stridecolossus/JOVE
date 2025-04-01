package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Function;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleNativeTransformer;

/**
 * A <i>native method</i> composes a native method and transformers for the return type and parameters.
 * @author Sarge
 */
public class NativeMethod {
	@SuppressWarnings("rawtypes")
	private final Function returns;
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
			throw new IllegalArgumentException("Mismatched number of transforms for method signature");
		}
		if(Objects.isNull(returns) ^ (type.returnType() == void.class)) {
			throw new IllegalArgumentException("Missing or unused returns transformer");
		}

		this.handle = requireNonNull(handle);
		this.returns = validate(returns); // TODO - filterReturnValue?
		this.signature = List.copyOf(signature);
	}

	// TODO
	private static Function<? extends Object, ?> validate(Transformer returns) {
//		if(returns == null) {
//			return null;
//		}
//		else {
			return switch(returns) {
				case null -> Function.identity();
    			case IdentityTransformer _ -> Function.identity();
    			case DefaultTransformer<?> def -> def.unmarshal();
    			case NativeStructureTransformer structure -> {
    				throw new UnsupportedOperationException(); // TODO
    			}
			};
//		}
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
//			@SuppressWarnings("resource")
			//final Object[] array =
			marshal(args, Arena.ofAuto());
			final Object result = handle.invokeWithArguments(args);
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
	private void marshal(Object[] args, Arena arena) {
		if(args == null) {
			return;
		}

//		final Object[] mapped = new Object[args.length];
//		for(int n = 0; n < args.length; ++n) {
//			mapped[n] = marshal(args[n], signature.get(n), arena);
//		}

		for(int n = 0; n < args.length; ++n) {
			switch(signature.get(n)) {
				case IdentityTransformer _ -> {}

				case DefaultTransformer def -> {

					if(args[n] == null) {
						args[n] = MemorySegment.NULL;
					}
					else {
						args[n] = def.marshal(args[n], arena);
					}
				}

				case NativeStructureTransformer transformer -> {
					final MemorySegment address = arena.allocate(transformer.layout());
					transformer.marshal((NativeStructure) args[n], address, arena);
					args[n] = address;
				}
			}
		}
	}

	/**
	 * Unmarshals the method return value.
	 * @param result Native return value
	 * @return Unmarshalled return value
	 */
	@SuppressWarnings("unchecked")
	private Object unmarshal(Object result) {
//		if(returns == null) {
//			assert result == null;
//			return null;
//		}
//		else {
			return returns.apply(result);
//		}
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

	//////////////////////

	public static class NativeMethodBuilder {
		private final Map<Class<?>, ValueLayout> primitives = Map.of(
				boolean.class,	JAVA_BOOLEAN,
				byte.class,		JAVA_BYTE,
				char.class,		JAVA_CHAR,
				short.class,	JAVA_SHORT,
				int.class,		JAVA_INT,
				long.class,		JAVA_LONG,
				float.class,	JAVA_FLOAT,
				double.class,	JAVA_DOUBLE
		);

		private final Linker linker = Linker.nativeLinker();
		private final NativeRegistry registry;

		/**
		 * Constructor.
		 * @param registry
		 */
		public NativeMethodBuilder(NativeRegistry registry) {
			this.registry = registry;
		}



		// TODO - turn inside out
		// local sum type
		// - primitive		<primitives>
		// - array			address
		// - structure		StructLayout
		// - reference		address
		// also handles mutation of return type and each argument?

		public MethodHandle build(MemorySegment address, MethodType signature) {
			final Class<?> type = signature.returnType();
			final MemoryLayout returns = type == void.class ? null : layout(type);

			final MemoryLayout[] parameters = signature
					.parameterList()
					.stream()
					.map(this::layout)
					.toArray(MemoryLayout[]::new);

			final FunctionDescriptor descriptor = descriptor(returns, parameters);

			//final
			MethodHandle handle = linker.downcallHandle(descriptor).bindTo(address);

			if(type == String.class) {
				System.out.println("  return string");
				try {
					final MethodHandle str = MethodHandles.lookup().findStatic(StringNativeTransformer.class, "unmarshal", MethodType.methodType(String.class, MemorySegment.class));
					handle = MethodHandles.filterReturnValue(handle, str);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}

			for(int n = 0; n < parameters.length; ++n) {
				if(parameters[n] == ValueLayout.ADDRESS) {
					if(signature.parameterArray()[n] == Handle.class) {
						System.out.println("  handle " + n);
						try {
							final var t = new HandleNativeTransformer();
							final MethodHandle h = MethodHandles.lookup().findVirtual(HandleNativeTransformer.class, "marshal", MethodType.methodType(MemorySegment.class, Handle.class));
							handle = MethodHandles.filterArguments(handle, n, h.bindTo(t));
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
					else
					if(NativeObject.class.isAssignableFrom(signature.parameterArray()[n])) {
						System.out.println("  object " + n);
						try {
							final MethodHandle h = MethodHandles.lookup().findStatic(NativeMethodBuilder.class, "object", MethodType.methodType(MemorySegment.class, NativeObject.class));
							handle = MethodHandles.filterArguments(handle, n, h);
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}



			System.out.println(handle);

			return handle;
		}

//		private static MemorySegment handle(Handle handle) {
//			return handle.address();
//		}

		private static MemorySegment object(NativeObject object) {
			return object.handle().address();
		}

		private MemoryLayout layout(Class<?> type) {
			if(type.isPrimitive()) {
				return primitives.get(type);
			}
			else
			if(type.isArray()) {
				// TODO
				throw new UnsupportedOperationException();
			}
			else
			if(NativeStructure.class.isAssignableFrom(type)) {
				// TODO
				throw new UnsupportedOperationException();
			}
			else {
				return ValueLayout.ADDRESS;
			}
		}

		private static FunctionDescriptor descriptor(MemoryLayout returns, MemoryLayout[] signature) {
			final FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(signature);
			if(returns == null) {
				return descriptor;
			}
			else {
				return descriptor.changeReturnLayout(returns);
			}
		}
	}

	//////////////////////

	//////////////////////

	/**
	 * Builder for a native method.
	 */
	public static class Builder {
		private final NativeRegistry registry;
		private final Linker linker;

		private MemorySegment address;
		private Transformer returns;
		private final List<Transformer> signature = new ArrayList<>();

		/**
		 * Constructor.
		 * @param registry Native transformers
		 */
		public Builder(NativeRegistry registry, Linker linker) {
			this.registry = requireNonNull(registry);
			this.linker = requireNonNull(linker);
		}

		/**
		 * Constructor.
		 * @param registry Native transformers
		 */
		public Builder(NativeRegistry registry) {
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
				this.returns = registry.transformer(type);
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
			final Transformer transformer = registry.transformer(type);
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
