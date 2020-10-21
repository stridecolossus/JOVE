package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"contents"
})
public class VkSubpassBeginInfoKHR extends VulkanStructure {
	public static class ByValue extends VkSubpassBeginInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassBeginInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_BEGIN_INFO_KHR;
	public Pointer pNext;
	public VkSubpassContents contents;
}
