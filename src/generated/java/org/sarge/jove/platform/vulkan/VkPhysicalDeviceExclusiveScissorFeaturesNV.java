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
	"exclusiveScissor"
})
public class VkPhysicalDeviceExclusiveScissorFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceExclusiveScissorFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExclusiveScissorFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_EXCLUSIVE_SCISSOR_FEATURES_NV;
	public Pointer pNext;
	public boolean exclusiveScissor;
}
