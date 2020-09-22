package org.sarge.jove.platform.vulkan;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>Vulkan boolean</i> is a custom JNA type wrapper for boolean values.
 * <p>
 * A Vulkan boolean class is mapped to a native <code>int</code> with a value that is explicitly 1 for <code>true</code> and 0 for <code>false</code>.
 * This circumvents the default JNA mapping for a Java <code>boolean</code> which is an <i>arbitrary non-zero</i> value for <code>true</code> (-1 in this case, Vulkan explicitly checks for <code>one</code>).
 * @author Sarge
 */
public final class VulkanBoolean {
	/**
	 * Boolean <tt>true</tt>.
	 */
	public static final VulkanBoolean TRUE = new VulkanBoolean(true);

	/**
	 * Boolean <tt>false</tt>.
	 */
	public static final VulkanBoolean FALSE = new VulkanBoolean(false);

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
	 * Converts a primitive boolean to a Vulkan boolean.
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
	 * @return Whether this boolean is <code>true</code>
	 */
	public boolean isTrue() {
		return value;
	}

	/**
	 * @return Vulkan integer representation (1 for <code>true</code>, 0 for <code>false</code>)
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
		return (obj instanceof VulkanBoolean b) && (this.value == b.value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
