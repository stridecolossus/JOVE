package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * Wrapper for the <i>device features</i> supported by a physical or logical device.
 * @see VkPhysicalDeviceFeatures
 * @author Sarge
 */
public class DeviceFeatures {
	private final VkPhysicalDeviceFeatures features;

	/**
	 * Constructor.
	 * @param features Supported features
	 */
	public DeviceFeatures(VkPhysicalDeviceFeatures features) {
		this.features = notNull(features);
		features.write(); // TODO - what is this doing?
	}

	/**
	 * Tests whether the given feature is present in this set.
	 * @param feature Feature name
	 * @return Whether present
	 */
	public boolean contains(String feature) {
		return features.readField(feature) == VulkanBoolean.TRUE;
	}

	/**
	 * @param required Required features
	 * @return Whether this set contains the given required features
	 */
	public boolean contains(Set<String> required) {
		return missing(required).isEmpty();
	}

	/**
	 * Enumerates features that are not supported in this set.
	 * @param required Required features
	 * @return Missing features
	 */
	public Set<String> missing(Set<String> required) {
		return required.stream().filter(Predicate.not(this::contains)).collect(toSet());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof DeviceFeatures that) && this.features.dataEquals(that.features);
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
