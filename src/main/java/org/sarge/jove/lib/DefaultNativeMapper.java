package org.sarge.jove.lib;

import java.lang.foreign.MemoryLayout;
import java.util.Objects;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * TODO
 * @author Sarge
 */
public class DefaultNativeMapper<T, R> extends AbstractNativeMapper<T> implements ReturnMapper<T, R> {
	/**
	 * Constructor.
	 * @param type			Java type
	 * @param layout		Native layout
	 */
	public DefaultNativeMapper(Class<T> type, MemoryLayout layout) {
		super(type, layout);
	}

	@Override
	public Object toNative(T value, NativeContext context) {
		assert Objects.nonNull(value);
		return value;
	}

	@Override
	public Object fromNative(R value, Class<? extends T> type) {
		return value;
	}
}
