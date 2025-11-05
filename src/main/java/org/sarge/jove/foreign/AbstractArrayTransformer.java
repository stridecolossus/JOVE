package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.Array;
import java.util.function.Function;

/**
 * Skeleton implementation that marshals an array to/from off-heap memory.
 * @author Sarge
 */
public abstract class AbstractArrayTransformer implements Transformer<Object, MemorySegment> {
	@SuppressWarnings("rawtypes")
	protected final Transformer component;

	/**
	 * Constructor.
	 * @param component Component transformer
	 */
	protected AbstractArrayTransformer(Transformer<?, ?> component) {
		this.component = requireNonNull(component);
	}

	@Override
	public final MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	@Override
	public MemorySegment marshal(Object array, SegmentAllocator allocator) {
		final MemoryLayout layout = component.layout();
		final int len = Array.getLength(array);
		final MemorySegment address = allocator.allocate(layout, len);
		marshal(array, len, address, allocator);
		return address;
	}

	/**
	 * Marshals an array to off-heap memory.
	 * @param array			Array
	 * @param length		Length
	 * @param address		Off-heap memory
	 * @param allocator		Allocator
	 */
	protected abstract void marshal(Object array, int length, MemorySegment address, SegmentAllocator allocator);

	@Override
	public final Function<MemorySegment, Object> unmarshal() {
		throw new UnsupportedOperationException();
	}
}
