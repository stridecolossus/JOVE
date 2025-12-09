package org.sarge.jove.platform.vulkan.common;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;

/**
 * The <i>device limits</i> is a wrapper for the mutable {@link VkPhysicalDeviceLimits} structure providing access to properties by name.
 * @author Sarge
 */
public class DeviceLimits {
	private final VkPhysicalDeviceLimits limits;

	/**
	 * Constructor.
	 * @param limits Device limits structure
	 */
	public DeviceLimits(VkPhysicalDeviceLimits limits) {
		this.limits = requireNonNull(limits);
	}

	/**
	 * Retrieves a device limit by name.
	 * @param <T> Field type
	 * @param name Property name
	 * @return Device limit
	 * @throws IllegalArgumentException if the property is unknown
	 * @throws RuntimeException if the property cannot be accessed
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		try {
			final Field field = VkPhysicalDeviceLimits.class.getField(name);
			return (T) field.get(limits);
		}
		catch(NoSuchFieldException e) {
			throw new IllegalArgumentException("Unknown device limits property: " + name);
		}
		catch(Exception e) {
			throw new RuntimeException("Cannot reflect device limits property: " + name, e);
		}
	}
}
