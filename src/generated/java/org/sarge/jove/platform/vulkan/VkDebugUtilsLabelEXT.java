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
	"pLabelName",
	"color"
})
public class VkDebugUtilsLabelEXT extends VulkanStructure {
	public static class ByValue extends VkDebugUtilsLabelEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsLabelEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_LABEL_EXT;
	public Pointer pNext;
	public String pLabelName;
	public float[] color = new float[4];
}
