package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * Specialised implementation that maps a Java {@code boolean} to a native integer.
 * <p>
 * This replaces the built-in {@link ValueLayout#JAVA_BOOLEAN} which represents a boolean value as a <b>one</b> byte value.
 * <p>
 * @author Sarge
 */
public record NativeBooleanTransformer() implements Transformer<Boolean, Integer> {
	/**
	 * Native {@code true} boolean.
	 */
	public static final int TRUE = 1;

	/**
	 * Native {@code false} boolean.
	 */
	public static final int FALSE = 0;

	/**
	 * @param value Boolean value as a native integer
	 * @return Whether the given value is {@link #TRUE}
	 */
	public static boolean isTrue(int value) {
		return value != FALSE;
	}

	@Override
	public MemoryLayout layout() {
		return JAVA_INT;
	}

	@Override
	public Integer marshal(Boolean value, SegmentAllocator allocator) {
		return value ? TRUE : FALSE;
	}

	@Override
	public Object empty() {
		// TODO - return FALSE? how do wrappers work exactly?
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<Integer, Boolean> unmarshal() {
		return NativeBooleanTransformer::isTrue;
	}

	@Override
	public Transformer<?, ?> array() {
		return PrimitiveTransformer.array(this, JAVA_INT);
	}
}
