package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.platform.vulkan.*;

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
	 */
	void populate(VkWriteDescriptorSet write);
}
