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
	"externalMemoryProperties"
})
public class VkExternalBufferProperties extends VulkanStructure {
	public VkStructureType sType = VkStructureType.EXTERNAL_BUFFER_PROPERTIES;
	public Pointer pNext;
	public VkExternalMemoryProperties externalMemoryProperties;
}
