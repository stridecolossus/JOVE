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
public class VkBaseInStructure extends Structure {
	public static class ByValue extends VkBaseInStructure implements Structure.ByValue { }
	public static class ByReference extends VkBaseInStructure implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BASE_IN_STRUCTURE.value();
	public VkBaseInStructure.ByReference pNext;
}
