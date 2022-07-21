package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * A set of <i>device features</i> enumerates the <i>supported</i> features of a physical device or the <i>required</i> features of the logical device.
 * @see VkPhysicalDeviceFeatures
 * @author Sarge
 */
public interface DeviceFeatures {
	/**
	 * @return Feature names
	 */
	Set<String> features();

	/**
	 * @return Descriptor for this set of features
	 */
	VkPhysicalDeviceFeatures descriptor();

	/**
	 * Tests whether this set contains the given features.
	 * @param features Required features
	 * @return Whether this set contains the given features
	 */
	boolean contains(DeviceFeatures features);

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractDeviceFeatures implements DeviceFeatures {
		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof DeviceFeatures that) &&
					this.features().equals(that.features());
		}
	}

	/**
	 * Empty set of features.
	 */
	DeviceFeatures EMPTY = new AbstractDeviceFeatures() {
		@Override
		public Set<String> features() {
			return Set.of();
		}

		@Override
		public VkPhysicalDeviceFeatures descriptor() {
			return null;
		}

		@Override
		public boolean contains(DeviceFeatures features) {
			return features.equals(this);
		}

		@Override
		public String toString() {
			return "EMPTY";
		}
	};

	/**
	 * Creates a set of <i>required</i> device features.
	 * @param required Required feature names
	 * @return Required device features
	 */
	static DeviceFeatures of(Collection<String> required) {
		return new AbstractDeviceFeatures() {
			@Override
			public Set<String> features() {
				return Set.copyOf(required);
			}

			@Override
			public VkPhysicalDeviceFeatures descriptor() {
				// Skip if empty
				if(required.isEmpty()) {
					return null;
				}

				// Build descriptor
				final var struct = new VkPhysicalDeviceFeatures();
				required.forEach(field -> struct.writeField(field, VulkanBoolean.TRUE));

				return struct;
			}

			@Override
			public boolean contains(DeviceFeatures features) {
				return required.containsAll(features.features());
			}

			@Override
			public String toString() {
				return required.toString();
			}
		};
	}

	/**
	 * Creates a set of <i>supported</i> device features from the given Vulkan descriptor.
	 * @param features Supported features
	 * @return Supported device features
	 */
	static DeviceFeatures of(VkPhysicalDeviceFeatures features) {
		// Init structure
		features.write();

		// Create wrapper
		return new AbstractDeviceFeatures() {
			@Override
			public Set<String> features() {
				return features
						.getFieldOrder()
						.stream()
						.filter(this::isEnabled)
						.collect(toSet());
			}

			@Override
			public VkPhysicalDeviceFeatures descriptor() {
				// TODO - mutable!
				return features;
			}

			@Override
			public boolean contains(DeviceFeatures required) {
				return required.features().stream().allMatch(this::isEnabled);
			}

			private boolean isEnabled(String field) {
				return features.readField(field) == VulkanBoolean.TRUE;
			}

			@Override
			public String toString() {
				return features().toString();
			}
		};
	}
}
