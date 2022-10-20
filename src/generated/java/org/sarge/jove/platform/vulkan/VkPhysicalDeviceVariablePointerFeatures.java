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
	"variablePointersStorageBuffer",
	"variablePointers"
})
public class VkPhysicalDeviceVariablePointerFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVariablePointerFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVariablePointerFeatures implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_VARIABLE_POINTER_FEATURES;
	public Pointer pNext;
	public boolean variablePointersStorageBuffer;
	public boolean variablePointers;
}
