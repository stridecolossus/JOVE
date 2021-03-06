package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"externalFenceFeatures"
})
public class VkExternalFenceProperties extends VulkanStructure {
	public static class ByValue extends VkExternalFenceProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalFenceProperties implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_FENCE_PROPERTIES;
	public Pointer pNext;
	public VkExternalFenceHandleTypeFlag exportFromImportedHandleTypes;
	public VkExternalFenceHandleTypeFlag compatibleHandleTypes;
	public VkExternalFenceFeatureFlag externalFenceFeatures;
}
