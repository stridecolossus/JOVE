package org.sarge.jove.util;

import java.lang.foreign.*;

import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.NativeMapper.ReturnMapper;

/**
 * The <i>bit mask native mapper</i> marshals an integer enumeration bit mask to/from its native integer representation.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class BitMaskNativeMapper extends AbstractNativeMapper<BitMask> implements ReturnMapper<BitMask, Integer> {
	/**
	 * Constructor.
	 */
	public BitMaskNativeMapper() {
		super(BitMask.class);
	}

	@Override
	public MemoryLayout layout(Class<? extends BitMask> type) {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Integer marshal(BitMask value, NativeContext __) {
		return value.bits();
	}

	@Override
	public Integer marshalNull(Class<? extends BitMask> type) {
		return 0;
	}

	@Override
	public BitMask<?> unmarshal(Integer value, Class<? extends BitMask> type) {
		return new BitMask<>(value);
	}
}
