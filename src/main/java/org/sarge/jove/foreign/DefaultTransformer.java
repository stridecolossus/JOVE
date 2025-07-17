package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * A <i>default transformer</i> is a template implementation to marshal a custom reference-type.
 * @param <T> Type
 * @author Sarge
 */
public non-sealed abstract class DefaultTransformer<T> implements Transformer {
	@Override
	public MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

    /**
	 * Marshals an argument to its native representation.
	 * @param arg			Argument
	 * @param allocator		Off-heap allocator
	 * @return Native argument
	 * @see #empty()
     */
    public abstract Object marshal(T arg, SegmentAllocator allocator);

    /**
	 * @return Empty or {@code null} argument
     */
    public Object empty() {
    	return MemorySegment.NULL;
    }

	/**
	 * Helper - Marshals a nullable reference-type argument.
	 * @param arg				Optional argument
	 * @param transformer		Transformer
	 * @param allocator			Off-heap memory
	 * @return Marshalled argument
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	static Object marshal(Object arg, DefaultTransformer transformer, SegmentAllocator allocator) {
		if(arg == null) {
			return transformer.empty();
		}
		else {
			return transformer.marshal(arg, allocator);
		}
	}

	/**
	 * @return Whether this transformer is used to marshal a by-reference parameter
	 */
	public boolean isReference() {
		return false;
	}

    /**
     * Provides a function to unmarshal a native return value of this type.
	 * @return Unmarshalling function
	 * @throws UnsupportedOperationException if this type cannot logically be returned from a native method
     */
    public Function<? extends Object, T> unmarshal() {
    	throw new UnsupportedOperationException();
    }
}
