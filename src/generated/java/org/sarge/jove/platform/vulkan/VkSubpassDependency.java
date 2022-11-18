package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

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
	public BitField<VkPipelineStage> srcStageMask;
	public BitField<VkPipelineStage> dstStageMask;
	public BitField<VkAccess> srcAccessMask;
	public BitField<VkAccess> dstAccessMask;
	public int dependencyFlags;
}
