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
	"computeBindingPointSupport"
})
public class VkDeviceGeneratedCommandsFeaturesNVX extends Structure {
	public static class ByValue extends VkDeviceGeneratedCommandsFeaturesNVX implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGeneratedCommandsFeaturesNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GENERATED_COMMANDS_FEATURES_NVX.value();
	public Pointer pNext;
	public boolean computeBindingPointSupport;
}
