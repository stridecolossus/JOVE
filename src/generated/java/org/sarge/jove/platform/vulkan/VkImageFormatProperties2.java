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
	"imageFormatProperties"
})
public class VkImageFormatProperties2 extends VulkanStructure {
	public static class ByValue extends VkImageFormatProperties2 implements Structure.ByValue { }
	public static class ByReference extends VkImageFormatProperties2 implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.IMAGE_FORMAT_PROPERTIES_2;
	public Pointer pNext;
	public VkImageFormatProperties imageFormatProperties;
}
