package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.function.Function;

public interface Callback {
	/**
	 *
	 */
	class CallbackTransformerFactory implements Registry.Factory<Callback> {
		@Override
		public Transformer<Callback, ?> transformer(Class<? extends Callback> type) {

			return new Transformer<Callback, MemorySegment>() {
				@Override
				public MemorySegment marshal(Callback callback, SegmentAllocator allocator) {
					// TODO

					MemorySegment stub = build(callback);

					System.err.println("marshal callback " + stub);
					return stub;
				}

				@Override
				public Function<MemorySegment, Callback> unmarshal() {
					throw new UnsupportedOperationException();
				}
			};
		}

		private static MemorySegment build(Callback callback) {

			try {
    			final MethodType type = MethodType.methodType(void.class, MemorySegment.class, double.class, double.class);
    			System.out.println(type);

    			final MethodHandle target = MethodHandles.lookup().findVirtual(callback.getClass(), "event", type).bindTo(callback);
    			System.out.println(target);

    			final var descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE);
    			System.out.println(descriptor);

    			final Linker linker = Linker.nativeLinker();
    			final MemorySegment stub = linker.upcallStub(target, descriptor, Arena.ofAuto());
    			System.out.println(stub);

    			return stub;
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
