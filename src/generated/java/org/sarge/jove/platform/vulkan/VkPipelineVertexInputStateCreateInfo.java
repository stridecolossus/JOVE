package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"vertexBindingDescriptionCount",
	"pVertexBindingDescriptions",
	"vertexAttributeDescriptionCount",
	"pVertexAttributeDescriptions"
})
public class VkPipelineVertexInputStateCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineVertexInputStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineVertexInputStateCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int vertexBindingDescriptionCount;
	public Pointer pVertexBindingDescriptions;
	public int vertexAttributeDescriptionCount;
	public Pointer pVertexAttributeDescriptions;
}
