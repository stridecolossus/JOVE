package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"handleTypes"
})
public class VkExternalMemoryBufferCreateInfo extends VulkanStructure {
	public static class ByValue extends VkExternalMemoryBufferCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExternalMemoryBufferCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.EXTERNAL_MEMORY_BUFFER_CREATE_INFO;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlag handleTypes;
}
