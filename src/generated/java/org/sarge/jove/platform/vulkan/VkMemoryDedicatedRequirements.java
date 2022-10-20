package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

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
public class VkMemoryDedicatedRequirements extends VulkanStructure {
	public static class ByValue extends VkMemoryDedicatedRequirements implements Structure.ByValue { }
	public static class ByReference extends VkMemoryDedicatedRequirements implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.MEMORY_DEDICATED_REQUIREMENTS;
	public Pointer pNext;
	public boolean prefersDedicatedAllocation;
	public boolean requiresDedicatedAllocation;
}
