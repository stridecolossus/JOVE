package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"attachmentCount",
	"pAttachments",
	"subpassCount",
	"pSubpasses",
	"dependencyCount",
	"pDependencies"
})
public class VkRenderPassCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.RENDER_PASS_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int attachmentCount;
	public VkAttachmentDescription pAttachments;
	public int subpassCount;
	public VkSubpassDescription pSubpasses;
	public int dependencyCount;
	public VkSubpassDependency pDependencies;
}
