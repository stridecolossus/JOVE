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
	"stage",
	"pCheckpointMarker"
})
public class VkCheckpointDataNV extends VulkanStructure {
	public static class ByValue extends VkCheckpointDataNV implements Structure.ByValue { }
	public static class ByReference extends VkCheckpointDataNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.CHECKPOINT_DATA_NV;
	public Pointer pNext;
	public VkPipelineStage stage;
	public Pointer pCheckpointMarker;
}
