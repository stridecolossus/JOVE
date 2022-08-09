package org.sarge.jove.platform.vulkan.util;

import com.sun.jna.*;

/**
 * A <i>Vulkan boolean</i> is a custom JNA type wrapper for boolean values used in the Vulkan API.
 * <p>
 * An instance of this class is mapped to a native {@code int} with a value that is <i>explicitly</i> integer one for {@code true} and zero for {@code false}.
 * This circumvents the default JNA mapping which is an <i>arbitrary non-zero</i> value for {@code true}.
 * <p>
 * @author Sarge
 */
public final class VulkanBoolean {
	/**
	 * Boolean {@code true} represented by integer one.
	 */
	public static final VulkanBoolean TRUE = new VulkanBoolean(1);

	/**
	 * Boolean {@code false} represented by integer zero.
	 */
	public static final VulkanBoolean FALSE = new VulkanBoolean(0);

	/**
	 * JNA type converter.
	 */
	public static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Integer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value == null) {
				return FALSE.value;
			}
			else {
				final VulkanBoolean bool = (VulkanBoolean) value;
				return bool.value;
			}
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			if(nativeValue == null) {
				return FALSE;
			}
			else {
				return of((int) nativeValue);
			}
		}
	};

	/**
	 * Converts a native integer value to a Vulkan boolean.
	 * @param value Native value
	 * @return Vulkan boolean
	 */
	public static VulkanBoolean of(int value) {
		return value == TRUE.value ? TRUE : FALSE;
	}

	/**
	 * Converts a primitive boolean to a Vulkan boolean.
	 * @param bool Boolean
	 * @return Vulkan boolean
	 */
	public static VulkanBoolean of(boolean bool) {
		return bool ? TRUE : FALSE;
	}

	private final int value;

	/**
	 * Constructor.
	 * @param value Underlying native value
	 */
	private VulkanBoolean(int value) {
		this.value = value;
	}

	/**
	 * @return Whether this boolean is {@link #TRUE}
	 */
	public boolean toBoolean() {
		return this == TRUE;
	}

	/**
	 * @return Native value
	 */
	public int toInteger() {
		return value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return String.valueOf(toBoolean());
	}
}
