package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext"
})
public class VkBaseOutStructure extends Structure {
	public static class ByValue extends VkBaseOutStructure implements Structure.ByValue { }
	public static class ByReference extends VkBaseOutStructure implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BASE_OUT_STRUCTURE.value();
	public VkBaseOutStructure.ByReference pNext;
}
