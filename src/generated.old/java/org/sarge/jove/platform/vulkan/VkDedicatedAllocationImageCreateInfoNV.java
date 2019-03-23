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
public class VkDedicatedAllocationImageCreateInfoNV extends Structure {
	public static class ByValue extends VkDedicatedAllocationImageCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkDedicatedAllocationImageCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_IMAGE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public boolean dedicatedAllocation;
}
