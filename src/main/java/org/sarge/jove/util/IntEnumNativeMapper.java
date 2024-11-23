package org.sarge.jove.util;

import java.lang.foreign.*;

import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.NativeMapper.ReturnMapper;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native mapper</i> marshals an enumeration to/from its native integer representation.
 * @author Sarge
 */
public class IntEnumNativeMapper extends AbstractNativeMapper<IntEnum> implements ReturnMapper<IntEnum, Integer> {
	/**
	 * Constructor.
	 */
	public IntEnumNativeMapper() {
		super(IntEnum.class);
	}

	@Override
	public MemoryLayout layout(Class<? extends IntEnum> type) {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Integer marshal(IntEnum e, NativeContext context) {
		return e.value();
	}

	@Override
	public Integer marshalNull(Class<? extends IntEnum> type) {
		return ReverseMapping
				.get(type)
				.defaultValue()
				.value();
	}

	@Override
	public IntEnum unmarshal(Integer value, Class<? extends IntEnum> type) {
		final ReverseMapping<?> mapping = ReverseMapping.get(type);
		if(value == 0) {
			return mapping.defaultValue();
		}
		else {
			return mapping.map(value);
		}
	}
}
