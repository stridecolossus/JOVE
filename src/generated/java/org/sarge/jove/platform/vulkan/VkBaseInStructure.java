package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext"
})
public class VkBaseInStructure extends VulkanStructure {
	public static class ByValue extends VkBaseInStructure implements Structure.ByValue { }
	public static class ByReference extends VkBaseInStructure implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BASE_IN_STRUCTURE;
	public Pointer pNext;
}
