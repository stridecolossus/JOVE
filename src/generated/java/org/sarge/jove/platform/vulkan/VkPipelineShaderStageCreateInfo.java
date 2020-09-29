package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"stage",
	"module",
	"pName",
	"pSpecializationInfo"
})
public class VkPipelineShaderStageCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineShaderStageCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineShaderStageCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkShaderStageFlag stage;
	public Handle module;
	public String pName = "main";
	public Pointer pSpecializationInfo;
}
