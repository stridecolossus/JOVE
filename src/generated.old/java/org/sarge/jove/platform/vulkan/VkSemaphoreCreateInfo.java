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
	"flags"
})
public class VkSemaphoreCreateInfo extends Structure {
	public static class ByValue extends VkSemaphoreCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkSemaphoreCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
}
