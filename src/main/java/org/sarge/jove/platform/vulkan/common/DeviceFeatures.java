package org.sarge.jove.platform.vulkan.common;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * The <i>device features</i> specifies the hardware features enabled by the application.
 * @author Sarge
 */
public class DeviceFeatures {
	private final Set<String> features;

	/**
	 * Constructor.
	 * @param features Enabled features
	 */
	public DeviceFeatures(Set<String> features) {
		this.features = Set.copyOf(features);
	}

	/**
	 * @return Enabled features
	 */
	public Set<String> enabled() {
		return features;
	}

	/**
	 * Builds the Vulkan structure for this set of required features.
	 * @return Device features
	 */
	public VkPhysicalDeviceFeatures structure() {
		final var struct = new VkPhysicalDeviceFeatures();
// TODO
//		for(String key : features) {
//			struct.writeField(key, Boolean.TRUE);
//		}
		return struct;
	}

	/**
	 * Tests whether the given feature is enabled on this device.
	 * @param feature Device feature
	 */
	public void require(String feature) {
		if(!features.contains(feature)) {
			throw new IllegalStateException("Feature not enabled by this device: " + feature);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DeviceFeatures that) &&
				this.features.equals(that.enabled());
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
