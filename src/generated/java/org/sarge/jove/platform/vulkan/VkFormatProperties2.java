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
	"formatProperties"
})
public class VkFormatProperties2 extends VulkanStructure {
	public VkStructureType sType = VkStructureType.FORMAT_PROPERTIES_2;
	public Pointer pNext;
	public VkFormatProperties formatProperties;
}
