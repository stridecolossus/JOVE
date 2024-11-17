package org.sarge.jove.lib;

import java.lang.foreign.ValueLayout;

import org.sarge.jove.util.BitMask;

/**
 * The <i>bit mask native mapper</i> marshals an integer enumeration bit mask to/from its native integer representation.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class BitMaskNativeMapper extends DefaultNativeMapper<BitMask, Integer> {
	/**
	 * Constructor.
	 */
	public BitMaskNativeMapper() {
		super(BitMask.class, ValueLayout.JAVA_INT);
	}

	@Override
	public Integer toNative(BitMask value, NativeContext __) {
		return value.bits();
	}

	@Override
	public Integer toNativeNull(Class<? extends BitMask> type) {
		return 0;
	}

	@Override
	public BitMask<?> fromNative(Integer value, Class<? extends BitMask> type) {
		return new BitMask<>(value);
	}
}
