package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.platform.vulkan.VkDescriptorType;

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
	 * Builds the descriptor for this resource.
	 * @return Resource descriptor
	 */
	NativeStructure descriptor();
}
