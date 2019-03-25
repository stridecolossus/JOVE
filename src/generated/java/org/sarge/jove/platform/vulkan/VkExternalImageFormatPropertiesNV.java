package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkExternalImageFormatPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkExternalImageFormatPropertiesNV implements Structure.ByReference { }
	
	public VkImageFormatProperties imageFormatProperties;
	public VkExternalMemoryFeatureFlagsNV externalMemoryFeatures;
	public VkExternalMemoryHandleTypeFlagsNV exportFromImportedHandleTypes;
	public VkExternalMemoryHandleTypeFlagsNV compatibleHandleTypes;
}