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
	"externalFenceFeatures"
})
public class VkExternalFenceProperties extends VulkanStructure {
	public VkStructureType sType = VkStructureType.EXTERNAL_FENCE_PROPERTIES;
	public Pointer pNext;
	public VkExternalFenceHandleTypeFlag exportFromImportedHandleTypes;
	public VkExternalFenceHandleTypeFlag compatibleHandleTypes;
	public VkExternalFenceFeatureFlag externalFenceFeatures;
}
