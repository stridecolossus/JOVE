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
	"waitSemaphoreCount",
	"pWaitSemaphores",
	"swapchainCount",
	"pSwapchains",
	"pImageIndices",
	"pResults"
})
public class VkPresentInfoKHR extends Structure {
	public static class ByValue extends VkPresentInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkPresentInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR.value();
	public Pointer pNext;
	public int waitSemaphoreCount;
	public long pWaitSemaphores;
	public int swapchainCount;
	public long pSwapchains;
	public int pImageIndices;
	public int pResults;
}
