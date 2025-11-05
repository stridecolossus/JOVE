package org.sarge.jove.platform.vulkan.common;

import static java.util.stream.Collectors.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

/**
 * A set of <i>device features</i> is a convenience wrapper specifying required or supported hardware features.
 * @see VkPhysicalDeviceFeatures
 * @author Sarge
 */
public record DeviceFeatures(Set<String> features) {
	/**
	 * Constructor.
	 * @param features Enabled features
	 */
	public DeviceFeatures {
		features = Set.copyOf(features);
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

	/**
	 * Creates a set of features from the given structure.
	 * @param features Device features structure
	 * @return Device features
	 */
	public static DeviceFeatures of(VkPhysicalDeviceFeatures features) {
		final Predicate<Field> enabled = field -> {
			try {
				return field.getBoolean(features);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		};

		return Arrays
				.stream(VkPhysicalDeviceFeatures.class.getFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.filter(enabled)
				.map(Field::getName)
				.collect(collectingAndThen(toSet(), DeviceFeatures::new));
	}
}
