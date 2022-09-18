package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"externalMemoryFeatures",
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes"
})
public class VkExternalMemoryProperties extends VulkanStructure {
	public VkExternalMemoryFeatureFlag externalMemoryFeatures;
	public VkExternalMemoryHandleTypeFlag exportFromImportedHandleTypes;
	public VkExternalMemoryHandleTypeFlag compatibleHandleTypes;
}
