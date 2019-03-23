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
	"cornerSampledImage"
})
public class VkPhysicalDeviceCornerSampledImageFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceCornerSampledImageFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceCornerSampledImageFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CORNER_SAMPLED_IMAGE_FEATURES_NV.value();
	public Pointer pNext;
	public boolean cornerSampledImage;
}
