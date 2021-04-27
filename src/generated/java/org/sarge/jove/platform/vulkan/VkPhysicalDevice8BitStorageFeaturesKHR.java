package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"storageBuffer8BitAccess",
	"uniformAndStorageBuffer8BitAccess",
	"storagePushConstant8"
})
public class VkPhysicalDevice8BitStorageFeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDevice8BitStorageFeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevice8BitStorageFeaturesKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES_KHR;
	public Pointer pNext;
	public VulkanBoolean storageBuffer8BitAccess;
	public VulkanBoolean uniformAndStorageBuffer8BitAccess;
	public VulkanBoolean storagePushConstant8;
}
