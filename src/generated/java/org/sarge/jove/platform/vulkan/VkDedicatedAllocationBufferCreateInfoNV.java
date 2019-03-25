package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"dedicatedAllocation"
})
public class VkDedicatedAllocationBufferCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkDedicatedAllocationBufferCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkDedicatedAllocationBufferCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_BUFFER_CREATE_INFO_NV;
	public Pointer pNext;
	public VulkanBoolean dedicatedAllocation;
}