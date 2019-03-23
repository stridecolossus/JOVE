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
	"handleTypes"
})
public class VkExternalMemoryImageCreateInfo extends Structure {
	public static class ByValue extends VkExternalMemoryImageCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExternalMemoryImageCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_IMAGE_CREATE_INFO.value();
	public Pointer pNext;
	public int handleTypes;
}
