package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"maxSampleLocationGridSize"
})
public class VkMultisamplePropertiesEXT extends Structure {
	public static class ByValue extends VkMultisamplePropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkMultisamplePropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MULTISAMPLE_PROPERTIES_EXT.value();
	public Pointer pNext;
	public VkExtent2D maxSampleLocationGridSize;
}
