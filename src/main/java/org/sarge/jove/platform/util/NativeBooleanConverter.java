package org.sarge.jove.platform.util;

import com.sun.jna.*;

/**
 * The <i>native boolean converter</i> is a custom JNA type converter for boolean values used by the native layer.
 * <p>
 * This converter marshals a Java boolean as a native integer that is <i>explicitly</i> integer one for {@code true} and zero for {@code false}.
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
		if(nativeValue == null) {
			return false;
		}
		else {
			final int n = (int) nativeValue;
			return n == TRUE;
		}
	}

	@Override
	public Integer toNative(Object value, ToNativeContext context) {
		if(value == null) {
			return FALSE;
		}
		else {
			final Boolean b = (Boolean) value;
			return b.booleanValue() ? TRUE : FALSE;
		}
	}
}
