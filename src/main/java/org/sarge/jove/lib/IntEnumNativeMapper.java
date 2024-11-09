package org.sarge.jove.lib;

import java.lang.foreign.*;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;
import org.sarge.jove.util.IntEnum;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native mapper</i> marshals an enumeration to/from its native integer representation.
 * @author Sarge
 */
public class IntEnumNativeMapper extends AbstractNativeMapper<IntEnum> implements ReturnMapper<Integer> {
	/**
	 * Constructor.
	 */
	public IntEnumNativeMapper() {
		super(IntEnum.class, ValueLayout.JAVA_INT);
	}

	@Override
	public Integer toNative(IntEnum e, Arena arena) {
		return e.value();
	}

	@Override
	public Object toNativeNull(Class<?> type) {
		return ReverseMapping
        		.get(type)
        		.defaultValue()
        		.value();
	}

	@Override
	public IntEnum fromNative(Integer value, Class<?> type) {
		final ReverseMapping<?> mapping = ReverseMapping.get(type);
		if(value == 0) {
			return mapping.defaultValue();
		}
		else {
			return mapping.map(value);
		}
	}
}
