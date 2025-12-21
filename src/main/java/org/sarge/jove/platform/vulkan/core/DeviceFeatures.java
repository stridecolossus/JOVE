package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * A set of <i>device features</i> is a convenience wrapper specifying required or supported hardware features by <i>name</i>.
 * @see VkPhysicalDeviceFeatures
 * @author Sarge
 */
public record DeviceFeatures(Set<String> features) implements Predicate<PhysicalDevice> {
	/**
	 * Constructor.
	 * @param features Enabled features
	 */
	public DeviceFeatures {
		features = Set.copyOf(features);
	}

	/**
	 * Helper.
	 * @param feature Feature name
	 * @return Whether this set contains the given feature
	 */
	public boolean contains(String feature) {
		return features.contains(feature);
	}

	/**
	 * Tests whether this is a subset of the given features.
	 * @param required Required features
	 * @return Whether all requires features are present
	 */
	public boolean contains(DeviceFeatures required) {
		return features.containsAll(required.features);
	}

	/**
	 * @return Device features structure
	 */
	public VkPhysicalDeviceFeatures build() {
		final var structure = new VkPhysicalDeviceFeatures();

		try {
    		for(String name : features) {
    			final Field field = VkPhysicalDeviceFeatures.class.getField(name);
    			field.set(structure, true);
    		}
		}
		catch(NoSuchFieldException e) {
			throw new IllegalArgumentException("Invalid device feature", e);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

		return structure;
	}

	@Override
	public boolean test(PhysicalDevice device) {
		return device.features().contains(this);
	}

	/**
	 * Creates a set of features from the given structure.
	 * @param features Device features structure
	 * @return Device features
	 */
	public static DeviceFeatures of(VkPhysicalDeviceFeatures features) {
		return Arrays
				.stream(VkPhysicalDeviceFeatures.class.getFields())
				.filter(field -> field.getType() == boolean.class)
				.filter(field -> isEnabled(features, field))
				.map(Field::getName)
				.collect(collectingAndThen(toSet(), DeviceFeatures::new));
	}

	private static boolean isEnabled(VkPhysicalDeviceFeatures features, Field field) {
		try {
			return field.getBoolean(features);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
