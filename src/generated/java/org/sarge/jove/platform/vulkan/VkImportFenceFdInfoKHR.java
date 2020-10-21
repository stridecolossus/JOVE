package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"fence",
	"flags",
	"handleType",
	"fd"
})
public class VkImportFenceFdInfoKHR extends VulkanStructure {
	public static class ByValue extends VkImportFenceFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImportFenceFdInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_FENCE_FD_INFO_KHR;
	public Pointer pNext;
	public Pointer fence;
	public int flags;
	public VkExternalFenceHandleTypeFlagBits handleType;
	public int fd;
}
