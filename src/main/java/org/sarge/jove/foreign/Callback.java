package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * A <i>callback</i> is a marker interface for an native upcall method.
 * @author Sarge
 */
public interface Callback {
	/**
	 * Transformer factory for callback methods.
	 */
	class CallbackTransformerFactory implements Registry.Factory<Callback> {
		private final Linker linker = Linker.nativeLinker();

		/**
		 * {@inheritDoc}
		 * @throws IllegalArgumentException if the callback is not a functional interface
		 * @throws IllegalArgumentException for an unsupported callback parameter
		 */
		@Override
		public Transformer<Callback, ?> transformer(Class<? extends Callback> type) {
			return new Transformer<Callback, MemorySegment>() {
				private final Method method = method(type);

				@Override
				public MemorySegment marshal(Callback callback, SegmentAllocator allocator) {
					return upcall(method, callback);
				}

				@Override
				public Function<MemorySegment, Callback> unmarshal() {
					throw new UnsupportedOperationException();
				}
			};
		}

		/**
		 * Retrieves the single callback method.
		 * @param callback Callback type
		 * @return Method
		 * @throws IllegalArgumentException if {@link #callback} is not a functional interface
		 */
		private static Method method(Class<? extends Callback> callback) {
			final Method[] methods = callback.getMethods();
			if(methods.length != 1) {
				throw new IllegalArgumentException("Expected functional interface: " + callback);
			}
			return methods[0];
		}

		/**
		 * Builds the upcall stub for the given callback method.
		 * @param callback Callback method
		 * @return Stub
		 * @throws IllegalArgumentException for an unsupported callback parameter
		 * @see FunctionalInterface
		 */
		private MemorySegment upcall(Method method, Callback instance) {
			final MethodHandle handle = handle(method);
			final MethodHandle binding = handle.bindTo(instance);
			final FunctionDescriptor function = function(method);
			return linker.upcallStub(binding, function, Arena.ofAuto());
		}

		/**
		 * @return Callback method handle
		 */
		private static MethodHandle handle(Method method) {
			try {
				return MethodHandles.lookup().unreflect(method);
			}
			catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		// TODO...

		/**
		 * @return Function descriptor for the given method
		 */
		private static FunctionDescriptor function(Method method) {

			final MemoryLayout[] layout = Arrays
					.stream(method.getParameterTypes())
					.map(CallbackTransformerFactory::map)
					.toArray(MemoryLayout[]::new);

			// TODO - return values

			return FunctionDescriptor.ofVoid(layout);
		}

		// TODO...
		private static MemoryLayout map(Class<?> type) {
			if(type == MemorySegment.class) {
				return ValueLayout.ADDRESS;
			}
			else
			if(type.isPrimitive()) {
				final ValueLayout[] primitives = {
			    		JAVA_BYTE,
			    		JAVA_CHAR,
			    		JAVA_SHORT,
			    		JAVA_INT,
			    		JAVA_LONG,
			    		JAVA_FLOAT,
			    		JAVA_DOUBLE
				};
				final Map<Class<?>, ValueLayout> map = Arrays
						.stream(primitives)
						.collect(toMap(ValueLayout::carrier, Function.identity()));
				final ValueLayout layout = map.get(type);
				if(layout == null) {
					throw new IllegalArgumentException("Unsupported primitive callback parameter type: " + type);
				}
				return layout;
			}
			else {
				throw new IllegalArgumentException("Unsupported callback parameter type: " + type);
			}
		}
	}

	// TODO - could we reflect the actual method, transform the received argument, and delegate? ~ native method?
	// TODO - factor out the native factory stuff that builds the transformer[] for parameters and reuse here
}
