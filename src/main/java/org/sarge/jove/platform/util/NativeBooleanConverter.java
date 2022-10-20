package org.sarge.jove.platform.util;

import com.sun.jna.*;

/**
 * The <i>native boolean converter</i> is a custom JNA type converter for boolean values used the native layer.
 * <p>
 * This converter marshals a boolean value as a native integer that is <i>explicitly</i> integer one for {@code true} and zero for {@code false}.
 * This overrides the default JNA mapping which assumes an <i>arbitrary non-zero</i> value for {@code true}.
 * <p>
 * @author Sarge
 */
public class NativeBooleanConverter implements TypeConverter {
	private static final int TRUE = 1;
	private static final int FALSE = 0;

	@Override
	public Class<?> nativeType() {
		return Integer.class;
	}

	@Override
	public Boolean fromNative(Object nativeValue, FromNativeContext context) {
		if(nativeValue instanceof Integer n) {
			return n == TRUE;
		}
		else {
			return false;
		}
	}

	@Override
	public Integer toNative(Object value, ToNativeContext context) {
		if(value instanceof Boolean b) {
			return b ? TRUE : FALSE;
		}
		else {
			return FALSE;
		}
	}
}
