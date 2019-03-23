package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_DEPENDENCY_2_KHR;
	public Pointer pNext;
	public int srcSubpass;
	public int dstSubpass;
	public VkPipelineStageFlags srcStageMask;
	public VkPipelineStageFlags dstStageMask;
	public VkAccessFlags srcAccessMask;
	public VkAccessFlags dstAccessMask;
	public VkDependencyFlags dependencyFlags;
	public int viewOffset;
}
