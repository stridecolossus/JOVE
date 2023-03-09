package org.sarge.jove.platform.vulkan.common;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * The <i>required features</i> specifies the device features required by the application.
 * @author Sarge
 */
public class RequiredFeatures {
	private final Set<String> features;

	/**
	 * Constructor.
	 * @param features Required features
	 */
	public RequiredFeatures(Set<String> features) {
		this.features = Set.copyOf(features);
	}

	/**
	 * @return Required features
	 */
	public Set<String> features() {
		return features;
	}

	/**
	 * Builds the Vulkan structure for this set of required features.
	 * @return Device features
	 */
	public VkPhysicalDeviceFeatures structure() {
		final var struct = new VkPhysicalDeviceFeatures();
		for(String key : features) {
			struct.writeField(key, Boolean.TRUE);
		}
		return struct;
	}

	/**
	 * Tests whether the given feature is enabled on this device.
	 * @param feature Required feature
	 */
	public void require(String feature) {
		if(!features.contains(feature)) {
			throw new IllegalStateException("Feature not supported by this device: " + feature);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof RequiredFeatures that) &&
				this.features.equals(that.features());
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
