package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * A <i>callback</i> is a marker interface for a native upcall method.
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
		 * @param registry Transformer registry
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
		public Transformer<Callback, MemorySegment> transformer(Class<? extends Callback> type) {
			return new Transformer<>() {
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
		 *
		 * TODO
		 * - integrate with transformer / registry
		 *
		 * thoughts:
		 * - use factory to create a proxy for the callback method?
		 * - then registry -> transformers for signature -> function descriptor
		 * - proxy delegates to reflected instance method of callback
		 *
		 * =>
		 *
		 * - callbacks specified in domain terms as native methods
		 * - method handle could/should be static?
		 * - no need for identity transformer for MemorySegment since would be Handle, Window, etc
		 *
		 */

		/**
		 * Builds the upcall stub for the given callback method.
		 * @param method		Callback method
		 * @param instance		Callback instance
		 * @return Upcall stub
		 * @throws IllegalArgumentException for an unsupported callback parameter
		 */
		@SuppressWarnings("resource")
		private MemorySegment upcall(Method method, Callback instance) {
			// Bind callback method
			final MethodHandle handle = handle(method).bindTo(instance);

			// Map return type
			final Mapper mapper = new Mapper(); // TODO - integrated with registry
			final Class<?> returnType = method.getReturnType();
			final MemoryLayout returns = returnType == void.class ? null : mapper.map(returnType);

			// Map parameters
			final MemoryLayout[] parameters = Arrays
					.stream(method.getParameterTypes())
					.map(mapper::map)
					.toArray(MemoryLayout[]::new);

			// Derive function descriptor
			final FunctionDescriptor descriptor = returns == null ? FunctionDescriptor.ofVoid(parameters) : FunctionDescriptor.of(returns, parameters);

			// Link callback stub
			return linker.upcallStub(handle, descriptor, Arena.global());
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

		// TODO - integrate with transformer/registry
		private static class Mapper {
			private final Map<Class<?>, MemoryLayout> map = new HashMap<>();

			public Mapper() {
				map.put(MemorySegment.class, AddressLayout.ADDRESS);

				final ValueLayout[] primitives = {
			    		JAVA_BYTE,
			    		JAVA_CHAR,
			    		JAVA_SHORT,
			    		JAVA_INT,
			    		JAVA_LONG,
			    		JAVA_FLOAT,
			    		JAVA_DOUBLE
				};
				for(ValueLayout layout : primitives) {
					final Class<?> carrier = layout.carrier();
					map.put(carrier, layout);
		    	}
			}

			public MemoryLayout map(Class<?> type) {
				final MemoryLayout layout = map.get(type);
				if(layout == null) {
					throw new IllegalArgumentException("Unsupported callback type: " + type);
				}
				return layout;
			}
		}
	}
}
