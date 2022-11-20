package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.*;

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
public class VkPipelineShaderStageCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO;
	public Pointer pNext;
	public int flags; // TODO - 1.3
	public VkShaderStage stage;
	public Handle module;
	public String pName;
	public VkSpecializationInfo pSpecializationInfo;
}
