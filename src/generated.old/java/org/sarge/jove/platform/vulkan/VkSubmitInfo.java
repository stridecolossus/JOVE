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
	"pWaitDstStageMask",
	"commandBufferCount",
	"pCommandBuffers",
	"signalSemaphoreCount",
	"pSignalSemaphores"
})
public class VkSubmitInfo extends Structure {
	public static class ByValue extends VkSubmitInfo implements Structure.ByValue { }
	public static class ByReference extends VkSubmitInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SUBMIT_INFO.value();
	public Pointer pNext;
	public int waitSemaphoreCount;
	public long pWaitSemaphores;
	public int pWaitDstStageMask;
	public int commandBufferCount;
	public Pointer pCommandBuffers;
	public int signalSemaphoreCount;
	public long pSignalSemaphores;
}
