package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"mipLevel",
	"arrayLayer"
})
public class VkImageSubresource extends VulkanStructure {
	public static class ByValue extends VkImageSubresource implements Structure.ByValue { }
	public static class ByReference extends VkImageSubresource implements Structure.ByReference { }

	public VkImageAspect aspectMask;
	public int mipLevel;
	public int arrayLayer;
}
