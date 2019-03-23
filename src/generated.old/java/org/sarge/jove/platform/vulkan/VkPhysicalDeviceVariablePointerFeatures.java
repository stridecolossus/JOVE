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
	"variablePointersStorageBuffer",
	"variablePointers"
})
public class VkPhysicalDeviceVariablePointerFeatures extends Structure {
	public static class ByValue extends VkPhysicalDeviceVariablePointerFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVariablePointerFeatures implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VARIABLE_POINTER_FEATURES.value();
	public Pointer pNext;
	public boolean variablePointersStorageBuffer;
	public boolean variablePointers;
}
