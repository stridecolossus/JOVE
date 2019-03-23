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
	"memoryRequirements"
})
public class VkSparseImageMemoryRequirements2 extends Structure {
	public static class ByValue extends VkSparseImageMemoryRequirements2 implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageMemoryRequirements2 implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SPARSE_IMAGE_MEMORY_REQUIREMENTS_2.value();
	public Pointer pNext;
	public VkSparseImageMemoryRequirements memoryRequirements;
}
