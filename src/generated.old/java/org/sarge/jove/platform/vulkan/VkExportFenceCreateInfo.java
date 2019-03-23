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
	"handleTypes"
})
public class VkExportFenceCreateInfo extends Structure {
	public static class ByValue extends VkExportFenceCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExportFenceCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXPORT_FENCE_CREATE_INFO.value();
	public Pointer pNext;
	public int handleTypes;
}
