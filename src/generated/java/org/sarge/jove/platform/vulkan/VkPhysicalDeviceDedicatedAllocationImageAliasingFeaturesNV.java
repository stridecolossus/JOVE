package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"dedicatedAllocationImageAliasing"
})
public class VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DEDICATED_ALLOCATION_IMAGE_ALIASING_FEATURES_NV;
	public Pointer pNext;
	public VulkanBoolean dedicatedAllocationImageAliasing;
}