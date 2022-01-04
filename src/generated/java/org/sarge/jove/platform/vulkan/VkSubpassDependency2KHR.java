package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"srcSubpass",
	"dstSubpass",
	"srcStageMask",
	"dstStageMask",
	"srcAccessMask",
	"dstAccessMask",
	"dependencyFlags",
	"viewOffset"
})
public class VkSubpassDependency2KHR extends VulkanStructure {
	public static class ByValue extends VkSubpassDependency2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDependency2KHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.SUBPASS_DEPENDENCY_2_KHR;
	public Pointer pNext;
	public int srcSubpass;
	public int dstSubpass;
	public VkPipelineStage srcStageMask;
	public VkPipelineStage dstStageMask;
	public VkAccess srcAccessMask;
	public VkAccess dstAccessMask;
	public VkDependencyFlag dependencyFlags;
	public int viewOffset;
}
