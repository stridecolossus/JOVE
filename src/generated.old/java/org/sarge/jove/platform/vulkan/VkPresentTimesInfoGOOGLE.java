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
	"swapchainCount",
	"pTimes"
})
public class VkPresentTimesInfoGOOGLE extends Structure {
	public static class ByValue extends VkPresentTimesInfoGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkPresentTimesInfoGOOGLE implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PRESENT_TIMES_INFO_GOOGLE.value();
	public Pointer pNext;
	public int swapchainCount;
	public VkPresentTimeGOOGLE.ByReference pTimes;
}
