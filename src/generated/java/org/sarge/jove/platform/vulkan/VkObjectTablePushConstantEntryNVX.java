package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"flags",
	"pipelineLayout",
	"stageFlags"
})
public class VkObjectTablePushConstantEntryNVX extends VulkanStructure {
	public static class ByValue extends VkObjectTablePushConstantEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTablePushConstantEntryNVX implements Structure.ByReference { }

	public VkObjectEntryTypeNVX type;
	public int flags;
	public Pointer pipelineLayout;
	public VkShaderStage stageFlags;
}
