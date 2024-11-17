package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import org.sarge.jove.util.IntEnum;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native mapper</i> marshals an enumeration to/from its native integer representation.
 * @author Sarge
 */
public class IntEnumNativeMapper extends DefaultNativeMapper<IntEnum, Integer> {
	/**
	 * Constructor.
	 */
	public IntEnumNativeMapper() {
		super(IntEnum.class, JAVA_INT);
	}

	@Override
	public Integer toNative(IntEnum e, NativeContext context) {
		return e.value();
	}

	@Override
	public Integer toNativeNull(Class<? extends IntEnum> type) {
		return ReverseMapping
				.get(type)
				.defaultValue()
				.value();
	}

	@Override
	public IntEnum fromNative(Integer value, Class<? extends IntEnum> type) {
		final ReverseMapping<?> mapping = ReverseMapping.get(type);
		if(value == 0) {
			return mapping.defaultValue();
		}
		else {
			return mapping.map(value);
		}
	}
}
