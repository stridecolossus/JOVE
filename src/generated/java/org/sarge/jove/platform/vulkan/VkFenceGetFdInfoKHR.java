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
	"handleType"
})
public class VkFenceGetFdInfoKHR extends VulkanStructure {
	public static class ByValue extends VkFenceGetFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkFenceGetFdInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_FENCE_GET_FD_INFO_KHR;
	public Pointer pNext;
	public Pointer fence;
	public VkExternalFenceHandleTypeFlagBits handleType;
}
