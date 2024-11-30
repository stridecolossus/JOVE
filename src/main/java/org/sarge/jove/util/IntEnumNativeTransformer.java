package org.sarge.jove.util;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.AbstractNativeTransformer;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native transform</i> maps an enumeration to/from its native integer representation.
 * @author Sarge
 */
public class IntEnumNativeTransformer extends AbstractNativeTransformer<IntEnum, Integer> {
	@Override
	public Class<IntEnum> type() {
		return IntEnum.class;
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Object transform(IntEnum e, SegmentAllocator allocator) {
		return e.value();
	}

	@Override
	public IntEnumNativeTransformer derive(Class<? extends IntEnum> target) {
		return new IntEnumNativeTransformer() {
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
