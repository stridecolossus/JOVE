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
	"flags",
	"buffer",
	"format",
	"offset",
	"range"
})
public class VkBufferViewCreateInfo extends VulkanStructure {
	public static class ByValue extends VkBufferViewCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkBufferViewCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.BUFFER_VIEW_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public Pointer buffer;
	public VkFormat format;
	public long offset;
	public long range;
}
