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
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes",
	"externalFenceFeatures"
})
public class VkExternalFenceProperties extends Structure {
	public static class ByValue extends VkExternalFenceProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalFenceProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXTERNAL_FENCE_PROPERTIES.value();
	public Pointer pNext;
	public int exportFromImportedHandleTypes;
	public int compatibleHandleTypes;
	public int externalFenceFeatures;
}
