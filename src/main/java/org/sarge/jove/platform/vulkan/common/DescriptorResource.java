package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.platform.vulkan.VkDescriptorType;

import com.sun.jna.Structure;

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
	 * Builds the Vulkan descriptor for this resource.
	 * @return Descriptor
	 */
	Structure populate();
}
