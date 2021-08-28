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
	"pNext"
})
public class VkBaseOutStructure extends VulkanStructure {
	public static class ByValue extends VkBaseOutStructure implements Structure.ByValue { }
	public static class ByReference extends VkBaseOutStructure implements Structure.ByReference { }

	//public VkStructureType sType = VkStructureType.BASE_OUT_STRUCTURE;
	public Pointer pNext;
}
