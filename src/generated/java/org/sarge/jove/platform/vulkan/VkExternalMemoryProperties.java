package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkExternalMemoryProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalMemoryProperties implements Structure.ByReference { }
	
	public VkExternalMemoryFeatureFlags externalMemoryFeatures;
	public VkExternalMemoryHandleTypeFlags exportFromImportedHandleTypes;
	public VkExternalMemoryHandleTypeFlags compatibleHandleTypes;
}