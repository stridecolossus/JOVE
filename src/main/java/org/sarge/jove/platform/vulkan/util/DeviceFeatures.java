package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.util.Check.notNull;

import java.util.Set;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

/**
 * A <i>device features</i> specifies the Vulkan features supported by a physical or logical device.
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
		final var struct = new VkPhysicalDeviceFeatures();
		for(String str : features) {
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
		features.write();
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
	public void check(VkPhysicalDeviceFeatures required) {
		// Init required features
		required.write();

		// Enumerate unsupported features
		final var missing = VulkanStructure.names(required)
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
