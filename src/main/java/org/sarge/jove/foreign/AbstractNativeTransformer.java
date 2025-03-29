package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * Partial implementation for a reference type.
 * @param <T> Java reference type
 * @author Sarge
 */
public abstract class AbstractNativeTransformer<T extends Object> implements NativeTransformer<T> {
	@Override
	public final MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	@Override
	public Function<MemorySegment, T> unmarshal() {
		throw new UnsupportedOperationException();
	}
}
