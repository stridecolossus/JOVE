package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext"
})
public class VkBaseInStructure extends VulkanStructure {
	//public VkStructureType sType = VkStructureType.BASE_IN_STRUCTURE;
	public Pointer pNext;
}
