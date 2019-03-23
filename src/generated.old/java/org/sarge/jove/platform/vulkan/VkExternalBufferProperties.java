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
	"externalMemoryProperties"
})
public class VkExternalBufferProperties extends Structure {
	public static class ByValue extends VkExternalBufferProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalBufferProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_BUFFER_PROPERTIES.value();
	public Pointer pNext;
	public VkExternalMemoryProperties externalMemoryProperties;
}
