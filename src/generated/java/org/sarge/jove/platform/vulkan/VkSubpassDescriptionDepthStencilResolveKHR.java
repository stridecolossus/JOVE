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
	"depthResolveMode",
	"stencilResolveMode",
	"pDepthStencilResolveAttachment"
})
public class VkSubpassDescriptionDepthStencilResolveKHR extends VulkanStructure {
	public static class ByValue extends VkSubpassDescriptionDepthStencilResolveKHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDescriptionDepthStencilResolveKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_DEPTH_STENCIL_RESOLVE_KHR;
	public Pointer pNext;
	public VkResolveModeFlagKHR depthResolveMode;
	public VkResolveModeFlagKHR stencilResolveMode;
	public Pointer pDepthStencilResolveAttachment;
}
