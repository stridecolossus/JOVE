package org.sarge.jove.platform.vulkan;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>Vulkan boolean</i> is a custom JNA type wrapper for boolean values.
 * <p>
 * A Vulkan boolean class is mapped to a native <tt>int</tt> with a value that is explicitly 1 for <tt>true</tt> and 0 for <tt>false</tt>.
 * This circumvents the default JNA mapping for a Java <tt>boolean</tt> which is an <i>arbitrary non-zero</i> value for <tt>true</tt> (-1 in this case, Vulkan explicitly checks for <tt>one</tt>).
 * @author Sarge
 */
public final class VulkanBoolean {
	/**
	 * JNA type converter.
	 */
	static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Integer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value == null) {
				return VulkanBoolean.FALSE.toInteger();
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
				return VulkanBoolean.of((int) nativeValue);
			}
		}
	};

	/**
	 * Boolean <tt>true</tt>.
	 */
	public static final VulkanBoolean TRUE = new VulkanBoolean(true);

	/**
	 * Boolean <tt>false</tt>.
	 */
	public static final VulkanBoolean FALSE = new VulkanBoolean(false);

	/**
	 * Converts a boolean represented as a native <tt>int</tt>.
	 * @param value Native value
	 * @return Vulkan boolean
	 */
	public static VulkanBoolean of(int value) {
		if(value == 0) {
			return VulkanBoolean.FALSE;
		}
		else {
			return VulkanBoolean.TRUE;
		}
	}

	/**
	 * Converts a boolean to a Vulkan boolean.
	 * @param bool Boolean
	 * @return Vulkan boolean
	 */
	public static VulkanBoolean of(boolean bool) {
		if(bool) {
			return VulkanBoolean.TRUE;
		}
		else {
			return VulkanBoolean.FALSE;
		}
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
	 * @return Whether this boolean is <tt>true</tt>
	 */
	public boolean isTrue() {
		return value;
	}

	/**
	 * @return Vulkan integer representation (1 for <tt>true</tt>, 0 for <tt>false</tt>)
	 */
	public int toInteger() {
		return value ? 1 : 0;
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VulkanBoolean) {
			return (this == TRUE) == (obj == TRUE);
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
