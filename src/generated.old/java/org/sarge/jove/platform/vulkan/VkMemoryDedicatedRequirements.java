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
	"prefersDedicatedAllocation",
	"requiresDedicatedAllocation"
})
public class VkMemoryDedicatedRequirements extends Structure {
	public static class ByValue extends VkMemoryDedicatedRequirements implements Structure.ByValue { }
	public static class ByReference extends VkMemoryDedicatedRequirements implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_DEDICATED_REQUIREMENTS.value();
	public Pointer pNext;
	public boolean prefersDedicatedAllocation;
	public boolean requiresDedicatedAllocation;
}
