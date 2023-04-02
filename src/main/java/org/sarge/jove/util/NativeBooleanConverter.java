package org.sarge.jove.util;

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
	public static final int TRUE = 1;
	public static final int FALSE = 0;

	/**
	 * Converts the given native integer to a Java boolean, where a non-zero value is {@code true}.
	 * @param value Native boolean value
	 * @return Boolean
	 */
	public static boolean of(int value) {
		return value != FALSE;
	}

	@Override
	public Class<?> nativeType() {
		return Integer.class;
	}

	@Override
	public Boolean fromNative(Object nativeValue, FromNativeContext context) {
		if(nativeValue instanceof Integer n) {
			return of(n);
		}
		else {
			return false;
		}
	}

	@Override
	public Integer toNative(Object value, ToNativeContext context) {
		if(value instanceof Boolean b) {
			return b.booleanValue() ? TRUE : FALSE;
		}
		else {
			return FALSE;
		}
	}
}
