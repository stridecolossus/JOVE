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
	"flags",
	"pipelineBindPoint",
	"inputAttachmentCount",
	"pInputAttachments",
	"colorAttachmentCount",
	"pColorAttachments",
	"pResolveAttachments",
	"pDepthStencilAttachment",
	"preserveAttachmentCount",
	"pPreserveAttachments"
})
public class VkSubpassDescription extends VulkanStructure {
	public static class ByValue extends VkSubpassDescription implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDescription implements Structure.ByReference { }

	public int flags;
	public VkPipelineBindPoint pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS;
	public int inputAttachmentCount;
	public Pointer pInputAttachments;
	public int colorAttachmentCount;
	public Pointer pColorAttachments;
	public Pointer pResolveAttachments;
	public VkAttachmentReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Pointer pPreserveAttachments;
}
