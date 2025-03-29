package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * An <i>identity native transformer</i> passes an argument as-is.
 * @param <T> Primitive or Java type
 * @author Sarge
 */
public record IdentityNativeTransformer<T>(MemoryLayout layout) implements NativeTransformer<T> {
	/**
	 * Constructor.
	 * @param layout Native memory layout
	 */
	public IdentityNativeTransformer {
		requireNonNull(layout);
	}

	@Override
	public Object marshal(Object arg, SegmentAllocator allocator) {
		return arg;
	}

//	@Override
//	public Object empty() {
//		// TODO - is this OK? should never happen for primitives anyway?
//		return MemorySegment.NULL;
//	}

	@Override
	public Function<T, T> unmarshal() {
		return Function.identity();
	}
}
