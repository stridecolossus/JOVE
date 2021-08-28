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
	"externalMemoryProperties"
})
public class VkExternalBufferProperties extends VulkanStructure {
	public static class ByValue extends VkExternalBufferProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalBufferProperties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.EXTERNAL_BUFFER_PROPERTIES;
	public Pointer pNext;
	public VkExternalMemoryProperties externalMemoryProperties;
}
