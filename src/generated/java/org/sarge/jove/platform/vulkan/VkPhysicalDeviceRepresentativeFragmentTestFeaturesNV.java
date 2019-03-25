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
	"representativeFragmentTest"
})
public class VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_REPRESENTATIVE_FRAGMENT_TEST_FEATURES_NV;
	public Pointer pNext;
	public VulkanBoolean representativeFragmentTest;
}