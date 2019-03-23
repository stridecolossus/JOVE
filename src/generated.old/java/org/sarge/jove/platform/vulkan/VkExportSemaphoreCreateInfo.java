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
	"handleTypes"
})
public class VkExportSemaphoreCreateInfo extends Structure {
	public static class ByValue extends VkExportSemaphoreCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExportSemaphoreCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXPORT_SEMAPHORE_CREATE_INFO.value();
	public Pointer pNext;
	public int handleTypes;
}
