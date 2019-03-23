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
public class VkExternalMemoryImageCreateInfoNV extends Structure {
	public static class ByValue extends VkExternalMemoryImageCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkExternalMemoryImageCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_IMAGE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int handleTypes;
}
