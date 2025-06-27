package org.sarge.jove.platform.vulkan.common;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.*;
import java.util.*;

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
	}

	/**
	 * @return Feature names
	 */
	public Set<String> features() {
		final var names = new HashSet<String>();
		try {
    		for(Field field : VkPhysicalDeviceFeatures.class.getFields()) {

    			final int mods = field.getModifiers();
    			if(!Modifier.isPublic(mods) || Modifier.isStatic(mods)) {
    				continue;
    			}

    			if(field.getInt(features) == 1) {
    				names.add(field.getName());
    			}
    		}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		return names;
	}

	/**
	 * @param feature Device feature
	 * @return Whether the given feature is supported by the hardware
	 */
	public boolean isEnabled(String feature) {
		try {
			return VkPhysicalDeviceFeatures.class.getField(feature).getInt(features) == 1;
		}
		catch(NoSuchFieldException e) {
			throw new IllegalArgumentException("Unknown device feature: " + feature);
		}
		catch(Exception e) {
			throw new RuntimeException("Error accessing device feature: " + feature, e);
		}
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
