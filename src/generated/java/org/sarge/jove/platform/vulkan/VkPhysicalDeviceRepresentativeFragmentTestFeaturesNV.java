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
	"representativeFragmentTest"
})
public class VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_REPRESENTATIVE_FRAGMENT_TEST_FEATURES_NV;
	public Pointer pNext;
	public boolean representativeFragmentTest;
}
