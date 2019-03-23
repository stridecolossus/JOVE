package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"surfaceCounters"
})
public class VkSwapchainCounterCreateInfoEXT extends Structure {
	public static class ByValue extends VkSwapchainCounterCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkSwapchainCounterCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SWAPCHAIN_COUNTER_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int surfaceCounters;
}
