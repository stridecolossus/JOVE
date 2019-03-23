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
	"swapchain",
	"timeout",
	"semaphore",
	"fence",
	"deviceMask"
})
public class VkAcquireNextImageInfoKHR extends Structure {
	public static class ByValue extends VkAcquireNextImageInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkAcquireNextImageInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_ACQUIRE_NEXT_IMAGE_INFO_KHR.value();
	public Pointer pNext;
	public long swapchain;
	public long timeout;
	public long semaphore;
	public long fence;
	public int deviceMask;
}
