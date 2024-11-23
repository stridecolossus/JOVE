package org.sarge.jove.platform.vulkan.common;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * The <i>supported features</i> enumerates the device features supported by the hardware.
 * @see DeviceFeatures
 * @author Sarge
 */
public final class SupportedFeatures {
	private final VkPhysicalDeviceFeatures features;

	/**
	 * Constructor.
	 * @param features Supported device features
	 */
	public SupportedFeatures(VkPhysicalDeviceFeatures features) {
		this.features = requireNonNull(features);
//		this.features.write();
	}

	/**
	 * @return Feature names
	 */
	public Set<String> features() {
//		return features
//				.getFieldOrder()
//				.stream()
//				.filter(this::isEnabled)
//				.collect(toSet());
		// TODO
		return null;
	}

	/**
	 * @param feature Device feature
	 * @return Whether the given feature is supported by the hardware
	 */
	public boolean isEnabled(String feature) {
		//return features.readField(feature) == Boolean.TRUE;
		return false;
	}

	/**
	 * @param required Required features
	 * @return Whether the given required features are supported by the hardware
	 */
	public boolean contains(DeviceFeatures required) {
		return this.features().containsAll(required.enabled());
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
