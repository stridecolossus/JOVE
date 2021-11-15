package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"image"
})
public class VkImageMemoryRequirementsInfo2 extends VulkanStructure {
	public static class ByValue extends VkImageMemoryRequirementsInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkImageMemoryRequirementsInfo2 implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.IMAGE_MEMORY_REQUIREMENTS_INFO_2;
	public Pointer pNext;
	public Pointer image;
}
