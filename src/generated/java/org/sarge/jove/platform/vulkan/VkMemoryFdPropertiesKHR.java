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
	"memoryTypeBits"
})
public class VkMemoryFdPropertiesKHR extends VulkanStructure {
	public static class ByValue extends VkMemoryFdPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkMemoryFdPropertiesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.MEMORY_FD_PROPERTIES_KHR;
	public Pointer pNext;
	public int memoryTypeBits;
}
