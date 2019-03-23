package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes",
	"externalSemaphoreFeatures"
})
public class VkExternalSemaphoreProperties extends VulkanStructure {
	public static class ByValue extends VkExternalSemaphoreProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalSemaphoreProperties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_SEMAPHORE_PROPERTIES;
	public Pointer pNext;
	public VkExternalSemaphoreHandleTypeFlags exportFromImportedHandleTypes;
	public VkExternalSemaphoreHandleTypeFlags compatibleHandleTypes;
	public VkExternalSemaphoreFeatureFlags externalSemaphoreFeatures;
}
