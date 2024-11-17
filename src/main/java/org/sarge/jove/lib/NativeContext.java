package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * The <i>native context</i> is used during marshalling to/from the native layer.
 * @author Sarge
 */
public record NativeContext(SegmentAllocator allocator, NativeMapperRegistry registry) {

	public NativeContext() {
		this(Arena.ofAuto(), NativeMapperRegistry.create());
	}

	/**
	 * Helper - Marshals a native value that can optionally be {@code null}.
	 * @param mapper		Native mapper
	 * @param value			Value to marshal
	 * @return Native value
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Object toNative(NativeMapper mapper, Object value, Class<?> type) {
		if(value == null) {
			return mapper.toNativeNull(type);
		}
		else {
			return mapper.toNative(value, this);
		}
	}
}
