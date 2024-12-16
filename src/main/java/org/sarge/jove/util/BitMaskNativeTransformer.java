package org.sarge.jove.util;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.NativeTransformer;

/**
 * The <i>bit mask native mapper</i> converts a bit mask to/from its native integer representation.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public record BitMaskNativeTransformer() implements NativeTransformer<BitMask, Integer> {
	@Override
	public ValueLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Integer transform(BitMask value, ParameterMode parameter, SegmentAllocator allocator) {
		if(value == null) {
			return 0;
		}
		else {
			return value.bits();
		}
	}

	@Override
	public Function<Integer, BitMask> returns() {
		return BitMask::new;
	}
}
