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
	"dedicatedAllocation"
})
public class VkDedicatedAllocationBufferCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkDedicatedAllocationBufferCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkDedicatedAllocationBufferCreateInfoNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEDICATED_ALLOCATION_BUFFER_CREATE_INFO_NV;
	public Pointer pNext;
	public VulkanBoolean dedicatedAllocation;
}
