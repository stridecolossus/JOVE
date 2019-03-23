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
	"dedicatedAllocation"
})
public class VkDedicatedAllocationBufferCreateInfoNV extends Structure {
	public static class ByValue extends VkDedicatedAllocationBufferCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkDedicatedAllocationBufferCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_BUFFER_CREATE_INFO_NV.value();
	public Pointer pNext;
	public boolean dedicatedAllocation;
}
