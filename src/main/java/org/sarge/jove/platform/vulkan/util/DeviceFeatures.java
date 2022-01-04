package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * A set of <i>device features</i> enumerates the <i>supported</i> features of a physical device or the <i>required</i> features of the logical device.
 * @see VkPhysicalDeviceFeatures
 * @author Sarge
 */
public interface DeviceFeatures {
	/**
	 * @return Enabled features
	 */
	Collection<String> features();

	/**
	 * Tests whether this set contains the given features.
	 * @param features Required features
	 * @return Whether this set contains the given features
	 * @see #contains(String)
	 */
	boolean contains(DeviceFeatures features);

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractDeviceFeatures implements DeviceFeatures {
		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof DeviceFeatures that) && this.features().equals(that.features());
		}

		@Override
		public String toString() {
			return features().toString();
		}
	}

	/**
	 * Creates a set of <i>required</i> device features.
	 * @param required Required feature names
	 * @return New required device features
	 */
	static DeviceFeatures of(Collection<String> required) {
		return new AbstractDeviceFeatures() {
			@Override
			public Collection<String> features() {
				return required;
			}

			@Override
			public boolean contains(DeviceFeatures features) {
				return required.containsAll(features.features());
			}
		};
	}

	/**
	 * Creates a set of <i>supported</i> device features.
	 * @param features Supported features
	 * @return New supported device features
	 */
	static DeviceFeatures of(VkPhysicalDeviceFeatures features) {
		// Init structure
		features.write();

		// Create wrapper
		return new AbstractDeviceFeatures() {
			@Override
			public Collection<String> features() {
				return features
						.getFieldOrder()
						.stream()
						//.map(Field::getName)
						.filter(this::isEnabled)
						.collect(toSet());
			}

			@Override
			public boolean contains(DeviceFeatures required) {
				return required.features().stream().allMatch(this::isEnabled);
			}

			private boolean isEnabled(String field) {
				return features.readField(field) == VulkanBoolean.TRUE;
			}
		};
	}

	/**
	 * Helper - Populates a required features structure.
	 * @param required Required features
	 * @return Device features or {@code null} if the argument is {@code null}
	 */
	static VkPhysicalDeviceFeatures populate(DeviceFeatures required) {
		// Ignore if not specified
		if(required == null) {
			return null;
		}

		// Enumerate required features
		final var struct = new VkPhysicalDeviceFeatures();
		required
				.features()
				.stream()
				.forEach(field -> struct.writeField(field, VulkanBoolean.TRUE));

		return struct;
	}
}
