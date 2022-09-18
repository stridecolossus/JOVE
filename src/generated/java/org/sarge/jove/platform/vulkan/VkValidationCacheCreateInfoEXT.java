package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"initialDataSize",
	"pInitialData"
})
public class VkValidationCacheCreateInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.VALIDATION_CACHE_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public long initialDataSize;
	public Pointer pInitialData;
}
