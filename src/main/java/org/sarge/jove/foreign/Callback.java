package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;
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
		private final Registry registry;

		/**
		 * Constructor.
		 * @param registry Registry
		 */
		public CallbackTransformerFactory(Registry registry) {
			this.registry = requireNonNull(registry);
		}

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
//			final FunctionDescriptor function = function(method);
//			return linker.upcallStub(binding, function, Arena.ofAuto());
			return null; // TODO
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
	}
}
