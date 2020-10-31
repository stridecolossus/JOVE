package org.sarge.jove.platform.vulkan.util;

import java.util.function.Supplier;

import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;

import com.sun.jna.Structure;

/**
 * A <i>resource</i> defines an object that can be applied to a descriptor set.
 * @param <T> Resource descriptor type
 */
public interface Resource<T extends Structure> {
	/**
	 * @return Descriptor type
	 */
	VkDescriptorType type();

	/**
	 * @return Identity instance
	 */
	Supplier<T> identity();

	/**
	 * Populates the update descriptor for this resource.
	 * @param descriptor Update descriptor
	 */
	void populate(T descriptor);

	/**
	 * Adds this update to the given write descriptor.
	 * @param descriptor		Update descriptor
	 * @param write				Write descriptor
	 */
	void apply(T descriptor, VkWriteDescriptorSet write);
}