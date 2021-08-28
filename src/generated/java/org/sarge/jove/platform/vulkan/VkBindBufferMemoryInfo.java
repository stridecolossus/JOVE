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
	"buffer",
	"memory",
	"memoryOffset"
})
public class VkBindBufferMemoryInfo extends VulkanStructure {
	public static class ByValue extends VkBindBufferMemoryInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindBufferMemoryInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.BIND_BUFFER_MEMORY_INFO;
	public Pointer pNext;
	public Pointer buffer;
	public Pointer memory;
	public long memoryOffset;
}
