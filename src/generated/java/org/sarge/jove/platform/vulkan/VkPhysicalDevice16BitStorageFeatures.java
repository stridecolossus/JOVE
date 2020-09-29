package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"storageBuffer16BitAccess",
	"uniformAndStorageBuffer16BitAccess",
	"storagePushConstant16",
	"storageInputOutput16"
})
public class VkPhysicalDevice16BitStorageFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDevice16BitStorageFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevice16BitStorageFeatures implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_16_BIT_STORAGE_FEATURES;
	public Pointer pNext;
	public VulkanBoolean storageBuffer16BitAccess;
	public VulkanBoolean uniformAndStorageBuffer16BitAccess;
	public VulkanBoolean storagePushConstant16;
	public VulkanBoolean storageInputOutput16;
}
