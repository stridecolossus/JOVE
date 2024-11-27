package org.sarge.jove.util;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native mapper</i> marshals an enumeration to/from its native integer representation.
 * @author Sarge
 */
public class IntEnumNativeMapper extends AbstractNativeMapper<IntEnum, Integer> {
	@Override
	public Class<IntEnum> type() {
		return IntEnum.class;
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Object marshal(IntEnum e, SegmentAllocator allocator) {
		return e.value();
	}

	@Override
	public IntEnumNativeMapper derive(Class<? extends IntEnum> target, NativeMapperRegistry registry) {
		return new IntEnumNativeMapper() {
    		private final ReverseMapping<?> mapping = ReverseMapping.get(target);

    		@Override
        	public Integer empty() {
        		return mapping.defaultValue().value();
        	}

        	@Override
        	public Function<Integer, IntEnum> returns() {
        		return value -> {
        			if(value == 0) {
        				return mapping.defaultValue();
        			}
        			else {
        				return mapping.map(value);
        			}
        		};
        	}
        };
	}
}
