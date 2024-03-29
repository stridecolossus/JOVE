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
	"pNext"
})
public class VkSubpassEndInfoKHR extends VulkanStructure {
	public static class ByValue extends VkSubpassEndInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassEndInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.SUBPASS_END_INFO_KHR;
	public Pointer pNext;
}
