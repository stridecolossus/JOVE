package org.sarge.jove.platform.vulkan.common;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>Vulkan boolean</i> is a custom JNA type wrapper for boolean values.
 * <p>
 * An instance of this class is mapped to a native {@code int} with a value that is <i>explicitly</i> 1 for {@code true} and 0 for {@code false}.
 * This circumvents the default JNA mapping which is an <i>arbitrary non-zero</i> value for {@code true}.
 * <p>
 * @author Sarge
 */
public final class VulkanBoolean {
	/**
	 * Boolean {@code true}.
	 */
	public static final VulkanBoolean TRUE = new VulkanBoolean(true);

	/**
	 * Boolean {@code false}.
	 */
	public static final VulkanBoolean FALSE = new VulkanBoolean(false);

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
				return 0;
			}
			else {
				final VulkanBoolean bool = (VulkanBoolean) value;
				return bool.toInteger();
			}
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			if(nativeValue == null) {
				return VulkanBoolean.FALSE;
			}
			else {
				return of((int) nativeValue);
			}
		}
	};

	/**
	 * Converts a native integer value to a Vulkan boolean (non-zero is {@code true}).
	 * @param value Native value
	 * @return Vulkan boolean
	 */
	public static VulkanBoolean of(int value) {
		return value == 0 ? VulkanBoolean.FALSE : VulkanBoolean.TRUE;
	}

	/**
	 * Converts a primitive boolean to a Vulkan boolean.
	 * @param bool Boolean
	 * @return Vulkan boolean
	 */
	public static VulkanBoolean of(boolean bool) {
		return bool ? VulkanBoolean.TRUE : VulkanBoolean.FALSE;
	}

	private final boolean value;

	/**
	 * Constructor.
	 * @param value Underlying boolean value
	 */
	private VulkanBoolean(boolean value) {
		this.value = value;
	}

	/**
	 * @return Native integer representation of this boolean (1 for {@code true} or 0 for {@code false})
	 */
	private int toInteger() {
		return value ? 1 : 0;
	}

	/**
	 * @return Value of this boolean wrapper
	 */
	public boolean toBoolean() {
		return value;
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
