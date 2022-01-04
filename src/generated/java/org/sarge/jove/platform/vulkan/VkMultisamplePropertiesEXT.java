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
	"maxSampleLocationGridSize"
})
public class VkMultisamplePropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkMultisamplePropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkMultisamplePropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.MULTISAMPLE_PROPERTIES_EXT;
	public Pointer pNext;
	public VkExtent2D maxSampleLocationGridSize;
}
