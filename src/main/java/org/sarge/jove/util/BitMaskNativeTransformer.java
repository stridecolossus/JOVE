package org.sarge.jove.util;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.AbstractNativeTransformer;

/**
 * The <i>bit mask native mapper</i> maps an bit mask to/from its native integer representation.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class BitMaskNativeTransformer extends AbstractNativeTransformer<BitMask, Integer> {
	@Override
	public Class<BitMask> type() {
		return BitMask.class;
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Integer transform(BitMask value, SegmentAllocator allocator) {
		return value.bits();
	}

	@Override
	public Integer empty() {
		return 0;
	}

	@Override
	public Function<Integer, BitMask> returns() {
		return BitMask::new;
	}
}
