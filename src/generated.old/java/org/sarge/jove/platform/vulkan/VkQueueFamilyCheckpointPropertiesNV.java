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
	"checkpointExecutionStageMask"
})
public class VkQueueFamilyCheckpointPropertiesNV extends Structure {
	public static class ByValue extends VkQueueFamilyCheckpointPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkQueueFamilyCheckpointPropertiesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_QUEUE_FAMILY_CHECKPOINT_PROPERTIES_NV.value();
	public Pointer pNext;
	public int checkpointExecutionStageMask;
}
