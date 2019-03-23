package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"dedicatedAllocationImageAliasing"
})
public class VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DEDICATED_ALLOCATION_IMAGE_ALIASING_FEATURES_NV.value();
	public Pointer pNext;
	public boolean dedicatedAllocationImageAliasing;
}
