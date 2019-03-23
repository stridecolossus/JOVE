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
	"decodeModeSharedExponent"
})
public class VkPhysicalDeviceASTCDecodeFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceASTCDecodeFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceASTCDecodeFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ASTC_DECODE_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean decodeModeSharedExponent;
}
