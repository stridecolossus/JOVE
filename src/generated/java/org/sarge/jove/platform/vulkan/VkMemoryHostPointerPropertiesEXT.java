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
	"memoryTypeBits"
})
public class VkMemoryHostPointerPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkMemoryHostPointerPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkMemoryHostPointerPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.MEMORY_HOST_POINTER_PROPERTIES_EXT;
	public Pointer pNext;
	public int memoryTypeBits;
}
