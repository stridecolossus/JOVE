package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"vertexBindingDivisorCount",
	"pVertexBindingDivisors"
})
public class VkPipelineVertexInputDivisorStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineVertexInputDivisorStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineVertexInputDivisorStateCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PIPELINE_VERTEX_INPUT_DIVISOR_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public int vertexBindingDivisorCount;
	public Pointer pVertexBindingDivisors;
}
