package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"attachment",
	"layout"
})
public class VkAttachmentReference extends VulkanStructure implements ByReference {
	public int attachment;
	public VkImageLayout layout;
}
