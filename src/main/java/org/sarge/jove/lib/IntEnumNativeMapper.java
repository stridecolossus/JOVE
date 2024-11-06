package org.sarge.jove.lib;

import java.lang.foreign.*;

import org.sarge.jove.util.IntEnum;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native mapper</i> marshals an enumeration to/from its native integer representation.
 * TODO - defaults
 * @author Sarge
 */
public class IntEnumNativeMapper extends DefaultNativeMapper implements NativeTypeConverter<IntEnum, Integer> {
	/**
	 * Constructor.
	 */
	public IntEnumNativeMapper() {
		super(IntEnum.class, ValueLayout.JAVA_INT);
	}

	@Override
	public Integer toNative(IntEnum e, Class<?> type, Arena __) {
		if(e == null) {
			final ReverseMapping<?> mapping = ReverseMapping.get(type);
			return mapping.defaultValue().value();
		}
		else {
			return e.value();
		}
	}

	@Override
	public IntEnum fromNative(Integer value, Class<?> type) {
		final ReverseMapping<?> mapping = ReverseMapping.get(type);
		if((value == null) || (value == 0)) {
			return mapping.defaultValue();
		}
		else {
			return mapping.map(value);
		}
	}
}
