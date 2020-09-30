package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"topology",
	"primitiveRestartEnable"
})
public class VkPipelineInputAssemblyStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkPrimitiveTopology topology;
	public VulkanBoolean primitiveRestartEnable;
}
