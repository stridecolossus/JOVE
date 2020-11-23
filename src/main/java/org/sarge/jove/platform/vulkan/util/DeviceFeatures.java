package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

/**
 * A set of <i>device features</i> specifies the features supported by a physical or logical device.
 * @author Sarge
 */
public class DeviceFeatures {
	/**
	 * Creates a set of supported features.
	 * @param features Feature names
	 * @return New supported features
	 */
	public static DeviceFeatures of(Set<String> features) {
		final var struct = new VkPhysicalDeviceFeatures();
		for(String str : features) {
			struct.writeField(str, VulkanBoolean.TRUE);
		}
		return new DeviceFeatures(struct);
	}

	private final VkPhysicalDeviceFeatures features;

	/**
	 * Constructor.
	 * @param features Features descriptor
	 */
	public DeviceFeatures(VkPhysicalDeviceFeatures features) {
		this.features = features.copy();
		this.features.write();
	}

	/**
	 * @return Device features descriptor
	 */
	public VkPhysicalDeviceFeatures get() {
		return features.copy();
	}

	/**
	 * @param feature Feature name
	 * @return Whether the given feature is supported
	 * @throws IllegalArgumentException if the feature is unknown
	 */
	public boolean isSupported(String feature) {
		return features.readField(feature) == VulkanBoolean.TRUE;
	}

	/**
	 * Helper -
	 * @param feature
	 * @throws IllegalStateException
	 */
	public void check(String feature) throws IllegalStateException {
		if(!isSupported(feature)) {
			throw new IllegalStateException("Unsupported feature: " + feature);
		}
	}

	/**
	 * Checks that this set of features supports the given required features.
	 * @param required Required features
	 * @throws IllegalStateException if any required feature is not in this set of supported features
	 */
	public void check(DeviceFeatures required) {
		// Enumerate missing features
		final Field[] fields = VkPhysicalDeviceFeatures.class.getFields();
		final Collection<String> missing = Arrays.stream(fields)
				.filter(f -> get(f, required.features))
				.filter(f -> !get(f, this.features))
				.map(Field::getName)
				.collect(toList());

		// Check
		if(!missing.isEmpty()) {
			throw new IllegalStateException("Unsupported feature(s): " + missing);
		}
	}

	private static boolean get(Field field, VkPhysicalDeviceFeatures obj) {
		try {
			return field.get(obj) == VulkanBoolean.TRUE;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
