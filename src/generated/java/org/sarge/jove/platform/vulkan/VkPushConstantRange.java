package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"stageFlags",
	"offset",
	"size"
})
public class VkPushConstantRange extends VulkanStructure {
	public static class ByValue extends VkPushConstantRange implements Structure.ByValue { }
	public static class ByReference extends VkPushConstantRange implements Structure.ByReference { }
	
	public VkShaderStageFlags stageFlags;
	public int offset;
	public int size;
}
