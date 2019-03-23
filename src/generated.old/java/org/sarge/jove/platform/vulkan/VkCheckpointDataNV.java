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
	"stage",
	"pCheckpointMarker"
})
public class VkCheckpointDataNV extends Structure {
	public static class ByValue extends VkCheckpointDataNV implements Structure.ByValue { }
	public static class ByReference extends VkCheckpointDataNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_CHECKPOINT_DATA_NV.value();
	public Pointer pNext;
	public int stage;
	public Pointer pCheckpointMarker;
}
