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
	"decodeModeSharedExponent"
})
public class VkPhysicalDeviceASTCDecodeFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceASTCDecodeFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceASTCDecodeFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ASTC_DECODE_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean decodeModeSharedExponent;
}