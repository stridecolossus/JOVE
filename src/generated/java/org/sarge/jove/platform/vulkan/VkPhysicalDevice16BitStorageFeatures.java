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
	"storageBuffer16BitAccess",
	"uniformAndStorageBuffer16BitAccess",
	"storagePushConstant16",
	"storageInputOutput16"
})
public class VkPhysicalDevice16BitStorageFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDevice16BitStorageFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevice16BitStorageFeatures implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_16BIT_STORAGE_FEATURES;
	public Pointer pNext;
	public boolean storageBuffer16BitAccess;
	public boolean uniformAndStorageBuffer16BitAccess;
	public boolean storagePushConstant16;
	public boolean storageInputOutput16;
}
