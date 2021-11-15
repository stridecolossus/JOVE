package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"vertexBindingDescriptionCount",
	"pVertexBindingDescriptions",
	"vertexAttributeDescriptionCount",
	"pVertexAttributeDescriptions"
})
public class VkPipelineVertexInputStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int vertexBindingDescriptionCount;
	public VkVertexInputBindingDescription pVertexBindingDescriptions;
	public int vertexAttributeDescriptionCount;
	public VkVertexInputAttributeDescription pVertexAttributeDescriptions;
}
