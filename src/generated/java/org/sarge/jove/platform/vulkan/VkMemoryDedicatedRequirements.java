package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"prefersDedicatedAllocation",
	"requiresDedicatedAllocation"
})
public class VkMemoryDedicatedRequirements extends VulkanStructure {
	public static class ByValue extends VkMemoryDedicatedRequirements implements Structure.ByValue { }
	public static class ByReference extends VkMemoryDedicatedRequirements implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.MEMORY_DEDICATED_REQUIREMENTS;
	public Pointer pNext;
	public VulkanBoolean prefersDedicatedAllocation;
	public VulkanBoolean requiresDedicatedAllocation;
}
