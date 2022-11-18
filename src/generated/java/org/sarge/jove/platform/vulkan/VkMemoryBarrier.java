package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"srcAccessMask",
	"dstAccessMask"
})
public class VkMemoryBarrier extends VulkanStructure {
	public VkStructureType sType = VkStructureType.MEMORY_BARRIER;
	public Pointer pNext;
	public BitField<VkAccess> srcAccessMask;
	public BitField<VkAccess> dstAccessMask;
}
