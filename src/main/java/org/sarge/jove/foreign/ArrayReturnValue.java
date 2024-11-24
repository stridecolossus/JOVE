package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.*;

public class ArrayReturnValue<T> {
	private final MemorySegment address;
	private final MemoryLayout layout;
//	private final ReturnMapper<?, MemorySegment> mapper;

	ArrayReturnValue(MemorySegment address, MemoryLayout layout, NativeMapper<?, MemorySegment> mapper) {
		this.address = requireNonNull(address);
		this.layout = requireNonNull(layout);
		//this.mapper = requireNonNull(mapper);
	}

	public T[] array(int length, IntFunction<T[]> factory) {
		// Resize handle to array layout
		final long size = layout.byteSize();
		final MemorySegment segment = address.reinterpret(size * length);

		// Allocate array
		final T[] array = factory.apply(length);

		// Extract elements
		for(int n = 0; n < length; ++n) {
			final MemorySegment e = segment.getAtIndex(ValueLayout.ADDRESS, n);
			//array[n] = (T) mapper.unmarshal(e, null);
		}

		return array;
	}

	public static class ArrayReturnValueMapper extends AbstractNativeMapper<ArrayReturnValue, MemorySegment> {
		private final NativeMapperRegistry registry;

		/**
		 * Constructor.
		 * @param registry Native mappers
		 */
		public ArrayReturnValueMapper(NativeMapperRegistry registry) {
			this.registry = requireNonNull(registry);
		}

		@Override
		public Class<ArrayReturnValue> type() {
			return ArrayReturnValue.class;
		}

		@Override
		public Object marshal(ArrayReturnValue instance, NativeContext context) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object marshalNull(Class<? extends ArrayReturnValue> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Function<MemorySegment, ArrayReturnValue> returns(Class<? extends ArrayReturnValue> target) {
//			return address -> {
//    			final NativeMapper mapper = registry.mapper(target).orElseThrow(() -> new IllegalArgumentException("Unsupported return type: " + target));
//    			final MemoryLayout layout = mapper.layout(target);
//
//    			mapper.returns(target)
//
//    			final ReturnMapper<?, MemorySegment> returns = (ReturnMapper<?, MemorySegment>) mapper; // TODO
//    			return new ArrayReturnValue<>(address, layout, returns);
//			};
			return null;
		}
	}
}
