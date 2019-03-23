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
public class VkExternalMemoryBufferCreateInfo extends Structure {
	public static class ByValue extends VkExternalMemoryBufferCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExternalMemoryBufferCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_BUFFER_CREATE_INFO.value();
	public Pointer pNext;
	public int handleTypes;
}
