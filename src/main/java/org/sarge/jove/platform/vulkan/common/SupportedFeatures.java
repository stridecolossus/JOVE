package org.sarge.jove.platform.vulkan.common;

import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * The <i>supported features</i> enumerates the device features supported by the hardware.
 * @see RequiredFeatures
 * @author Sarge
 */
public final class SupportedFeatures {
	private final VkPhysicalDeviceFeatures features;

	/**
	 * Constructor.
	 * @param features Supported device features
	 */
	public SupportedFeatures(VkPhysicalDeviceFeatures features) {
		this.features = notNull(features);
		this.features.write();
	}

	/**
	 * @return Feature names
	 */
	public Set<String> features() {
		return features
				.getFieldOrder()
				.stream()
				.filter(this::isEnabled)
				.collect(toSet());
	}

	/**
	 * @param feature Device feature
	 * @return Whether the given feature is supported by the hardware
	 */
	public boolean isEnabled(String feature) {
		return features.readField(feature) == Boolean.TRUE;
	}

	/**
	 * @param required Required features
	 * @return Whether the given required features are supported by the hardware
	 */
	public boolean contains(RequiredFeatures required) {
		return this.features().containsAll(required.features());
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
