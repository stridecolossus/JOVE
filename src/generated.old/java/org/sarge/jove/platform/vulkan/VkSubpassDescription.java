package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.StructureHelper;

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
public class VkSubpassDescription extends Structure {
	public static class ByValue extends VkSubpassDescription implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDescription implements Structure.ByReference { }

	public int flags;
	public int pipelineBindPoint;
	public int inputAttachmentCount;
	public Pointer pInputAttachments;
	public int colorAttachmentCount;
	public Pointer pColorAttachments;
	public Pointer pResolveAttachments;
	public VkAttachmentReference.ByReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Pointer pPreserveAttachments;

	public static class Builder {
		// TODO
		public VkSubpassDescription build() {
			final VkSubpassDescription subpass = new VkSubpassDescription.ByReference();
			subpass.pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS.value();

			// TODO
			final VkAttachmentReference[] refs = new VkAttachmentReference[1];
			refs[0] = new VkAttachmentReference();
			refs[0].attachment = 0;
			refs[0].layout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL.value();

			subpass.colorAttachmentCount = 1;
			subpass.pColorAttachments = StructureHelper.allocate(refs);

			return subpass;
		}
	}
}
