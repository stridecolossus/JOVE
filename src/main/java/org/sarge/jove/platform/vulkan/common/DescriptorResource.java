package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;

/**
 * A <i>descriptor resource</i> defines an object that can be applied to a descriptor set.
 * @author Sarge
 */
public interface DescriptorResource {
	/**
	 * @return Descriptor type
	 */
	VkDescriptorType type();

	/**
	 * Populates the write descriptor for this resource.
	 * @param write Write descriptor
	 */
	void populate(VkWriteDescriptorSet write);
}
