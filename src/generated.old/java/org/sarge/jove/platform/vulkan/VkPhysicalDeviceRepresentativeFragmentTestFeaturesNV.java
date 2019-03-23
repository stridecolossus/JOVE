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
	"representativeFragmentTest"
})
public class VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_REPRESENTATIVE_FRAGMENT_TEST_FEATURES_NV.value();
	public Pointer pNext;
	public boolean representativeFragmentTest;
}
