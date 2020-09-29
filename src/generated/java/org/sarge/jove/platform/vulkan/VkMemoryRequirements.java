package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"size",
	"alignment",
	"memoryTypeBits"
})
public class VkMemoryRequirements extends VulkanStructure {
	public static class ByValue extends VkMemoryRequirements implements Structure.ByValue { }
	public static class ByReference extends VkMemoryRequirements implements Structure.ByReference { }
	
	public long size;
	public long alignment;
	public int memoryTypeBits;
}
