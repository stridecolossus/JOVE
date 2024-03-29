package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"imageFormatProperties",
	"externalMemoryFeatures",
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes"
})
public class VkExternalImageFormatPropertiesNV extends VulkanStructure {
	public VkImageFormatProperties imageFormatProperties;
	public VkExternalMemoryFeatureFlagBitsNV externalMemoryFeatures;
	public VkExternalMemoryHandleTypeFlagNV exportFromImportedHandleTypes;
	public VkExternalMemoryHandleTypeFlagNV compatibleHandleTypes;
}
