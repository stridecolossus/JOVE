package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * Wrapper for the <i>device features</i> supported by a physical or logical device.
 * @see VkPhysicalDeviceFeatures
 * @author Sarge
 */
public class DeviceFeatures {
	/**
	 * Creates a set of supported features.
	 * @param features Feature names
	 * @return New supported features
	 * @throws IllegalArgumentException if any feature name is unknown
	 * @see VkPhysicalDeviceFeatures
	 */
	public static DeviceFeatures of(Set<String> features) {
		final VkPhysicalDeviceFeatures struct = new VkPhysicalDeviceFeatures();
		for(final String str : features) {
			struct.writeField(str, VulkanBoolean.TRUE);
		}
		return new DeviceFeatures(struct);
	}

	/**
	 * @param features		Features
	 * @param name			Field name
	 * @return Whether the given feature is supported
	 */
	private static boolean isSupported(VkPhysicalDeviceFeatures features, String name) {
		return features.readField(name) == VulkanBoolean.TRUE;
	}

	private final VkPhysicalDeviceFeatures features;

	/**
	 * Constructor.
	 * @param features Features descriptor
	 */
	public DeviceFeatures(VkPhysicalDeviceFeatures features) {
		this.features = notNull(features);
		this.features.write();
	}

	/**
	 * @param name Feature name
	 * @return Whether the given feature is supported
	 * @throws IllegalArgumentException if the feature is unknown
	 */
	public boolean isSupported(String name) {
		return isSupported(features, name);
	}

	/**
	 * Tests whether the given required features are supported by this set of features.
	 * @param required Required features
	 */
	public void check(VkPhysicalDeviceFeatures required) {		// TODO - should be compare(DeviceFeatures):missing
		// Init required features
		required.write();

		// Enumerate unsupported features
		final var missing = required
				.getFieldList()
				.stream()
				.map(Field::getName)
				.filter(f -> isSupported(required, f))
				.filter(Predicate.not(this::isSupported))
				.collect(toList());

		// Check for unsupported features
		if(!missing.isEmpty()) {
			throw new IllegalArgumentException("Feature(s) not supported: " + missing);
		}
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
