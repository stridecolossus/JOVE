package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkRenderPassCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.RENDER_PASS_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int attachmentCount;
	public VkAttachmentDescription[] pAttachments;
	public int subpassCount;
	public VkSubpassDescription[] pSubpasses;
	public int dependencyCount;
	public VkSubpassDependency[] pDependencies;
}
