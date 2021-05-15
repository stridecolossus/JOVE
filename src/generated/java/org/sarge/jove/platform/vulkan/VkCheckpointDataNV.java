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
	"stage",
	"pCheckpointMarker"
})
public class VkCheckpointDataNV extends VulkanStructure {
	public static class ByValue extends VkCheckpointDataNV implements Structure.ByValue { }
	public static class ByReference extends VkCheckpointDataNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_CHECKPOINT_DATA_NV;
	public Pointer pNext;
	public VkPipelineStage stage;
	public Pointer pCheckpointMarker;
}
