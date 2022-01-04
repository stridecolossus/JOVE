package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"formatProperties",
	"imageMipTailFirstLod",
	"imageMipTailSize",
	"imageMipTailOffset",
	"imageMipTailStride"
})
public class VkSparseImageMemoryRequirements extends VulkanStructure {
	public static class ByValue extends VkSparseImageMemoryRequirements implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageMemoryRequirements implements Structure.ByReference { }
	
	public VkSparseImageFormatProperties formatProperties;
	public int imageMipTailFirstLod;
	public long imageMipTailSize;
	public long imageMipTailOffset;
	public long imageMipTailStride;
}
