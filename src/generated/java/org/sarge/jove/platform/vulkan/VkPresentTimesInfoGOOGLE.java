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
	"swapchainCount",
	"pTimes"
})
public class VkPresentTimesInfoGOOGLE extends VulkanStructure {
	public static class ByValue extends VkPresentTimesInfoGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkPresentTimesInfoGOOGLE implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PRESENT_TIMES_INFO_GOOGLE;
	public Pointer pNext;
	public int swapchainCount;
	public Pointer pTimes;
}
