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
	"computeBindingPointSupport"
})
public class VkDeviceGeneratedCommandsFeaturesNVX extends VulkanStructure {
	public static class ByValue extends VkDeviceGeneratedCommandsFeaturesNVX implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGeneratedCommandsFeaturesNVX implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEVICE_GENERATED_COMMANDS_FEATURES_NVX;
	public Pointer pNext;
	public VulkanBoolean computeBindingPointSupport;
}
