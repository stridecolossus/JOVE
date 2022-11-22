package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure.*;

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
	public BitMask<VkPipelineStage> srcStageMask;
	public BitMask<VkPipelineStage> dstStageMask;
	public BitMask<VkAccess> srcAccessMask;
	public BitMask<VkAccess> dstAccessMask;
	public BitMask<VkDependencyFlag> dependencyFlags;
}
