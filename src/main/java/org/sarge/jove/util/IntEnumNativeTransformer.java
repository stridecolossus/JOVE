package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.NativeTransformer;
import org.sarge.jove.foreign.TransformerRegistry.Factory;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * The <i>integer enumeration native transformer</i> converts an enumeration to/from its native integer representation.
 * @author Sarge
 */
public record IntEnumNativeTransformer(ReverseMapping<?> mapping) implements NativeTransformer<IntEnum, Integer> {
	/**
	 * Transformer factory for an integer enumeration.
	 */
	public static final Factory<IntEnum> FACTORY = (type, registry) -> {
		final ReverseMapping<?> mapping = ReverseMapping.get(type);
		return new IntEnumNativeTransformer(mapping);
	};

	/**
	 * Constructor.
	 * @param mapping Enumeration reverse mapping
	 */
	public IntEnumNativeTransformer {
		requireNonNull(mapping);
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Integer transform(IntEnum e, ParameterMode parameter, SegmentAllocator allocator) {
		if(e == null) {
			return mapping.defaultValue().value();
		}
		else {
			return e.value();
		}
	}

	@Override
	public Function<Integer, IntEnum> returns() {
		return value -> (value == 0) ? mapping.defaultValue() : mapping.map(value);
	}
}
