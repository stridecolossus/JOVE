package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"memory",
	"handleType"
})
public class VkMemoryGetFdInfoKHR extends VulkanStructure {
	public static class ByValue extends VkMemoryGetFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkMemoryGetFdInfoKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.MEMORY_GET_FD_INFO_KHR;
	public Pointer pNext;
	public Pointer memory;
	public VkExternalMemoryHandleTypeFlag handleType;
}
