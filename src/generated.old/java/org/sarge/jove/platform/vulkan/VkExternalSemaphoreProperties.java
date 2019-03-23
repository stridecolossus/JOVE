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
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes",
	"externalSemaphoreFeatures"
})
public class VkExternalSemaphoreProperties extends Structure {
	public static class ByValue extends VkExternalSemaphoreProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalSemaphoreProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_SEMAPHORE_PROPERTIES.value();
	public Pointer pNext;
	public int exportFromImportedHandleTypes;
	public int compatibleHandleTypes;
	public int externalSemaphoreFeatures;
}
