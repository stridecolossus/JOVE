package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DEVICE_GENERATED_COMMANDS_FEATURES_NVX;
	public Pointer pNext;
	public boolean computeBindingPointSupport;
}
