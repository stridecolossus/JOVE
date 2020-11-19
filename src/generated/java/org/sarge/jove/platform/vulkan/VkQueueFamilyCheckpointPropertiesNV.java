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
	"sType",
	"pNext",
	"checkpointExecutionStageMask"
})
public class VkQueueFamilyCheckpointPropertiesNV extends VulkanStructure {
	public static class ByValue extends VkQueueFamilyCheckpointPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkQueueFamilyCheckpointPropertiesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_QUEUE_FAMILY_CHECKPOINT_PROPERTIES_NV;
	public Pointer pNext;
	public VkPipelineStageFlag checkpointExecutionStageMask;
}
