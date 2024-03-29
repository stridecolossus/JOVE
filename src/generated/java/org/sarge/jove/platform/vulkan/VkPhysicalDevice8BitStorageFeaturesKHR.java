package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
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

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES_KHR;
	public Pointer pNext;
	public boolean storageBuffer8BitAccess;
	public boolean uniformAndStorageBuffer8BitAccess;
	public boolean storagePushConstant8;
}
