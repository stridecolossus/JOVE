package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"srcSubpass",
	"dstSubpass",
	"srcStageMask",
	"dstStageMask",
	"srcAccessMask",
	"dstAccessMask",
	"dependencyFlags"
})
public class VkSubpassDependency extends VulkanStructure implements ByReference {
	public int srcSubpass;
	public int dstSubpass;
	public int srcStageMask;
	public int dstStageMask;
	public int srcAccessMask;
	public int dstAccessMask;
	public int dependencyFlags;
}
