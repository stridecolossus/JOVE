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
	"semaphore",
	"handleType"
})
public class VkSemaphoreGetFdInfoKHR extends Structure {
	public static class ByValue extends VkSemaphoreGetFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSemaphoreGetFdInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SEMAPHORE_GET_FD_INFO_KHR.value();
	public Pointer pNext;
	public long semaphore;
	public int handleType;
}
