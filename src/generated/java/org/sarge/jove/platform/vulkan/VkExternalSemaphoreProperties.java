package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.EXTERNAL_SEMAPHORE_PROPERTIES;
	public Pointer pNext;
	public VkExternalSemaphoreHandleTypeFlag exportFromImportedHandleTypes;
	public VkExternalSemaphoreHandleTypeFlag compatibleHandleTypes;
	public VkExternalSemaphoreFeatureFlag externalSemaphoreFeatures;
}
