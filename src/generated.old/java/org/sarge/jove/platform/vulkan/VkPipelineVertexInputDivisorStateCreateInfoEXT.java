package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPipelineVertexInputDivisorStateCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineVertexInputDivisorStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineVertexInputDivisorStateCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_DIVISOR_STATE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int vertexBindingDivisorCount;
	public VkVertexInputBindingDivisorDescriptionEXT.ByReference pVertexBindingDivisors;
}
