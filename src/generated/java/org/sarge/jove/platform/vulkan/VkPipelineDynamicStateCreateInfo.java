package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"dynamicStateCount",
	"pDynamicStates"
})
public class VkPipelineDynamicStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int dynamicStateCount;
	public Pointer pDynamicStates;
}