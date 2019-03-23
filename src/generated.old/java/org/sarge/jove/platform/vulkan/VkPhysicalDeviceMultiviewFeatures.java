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
	"multiview",
	"multiviewGeometryShader",
	"multiviewTessellationShader"
})
public class VkPhysicalDeviceMultiviewFeatures extends Structure {
	public static class ByValue extends VkPhysicalDeviceMultiviewFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewFeatures implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_FEATURES.value();
	public Pointer pNext;
	public boolean multiview;
	public boolean multiviewGeometryShader;
	public boolean multiviewTessellationShader;
}
