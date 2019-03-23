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
	"disabledValidationCheckCount",
	"pDisabledValidationChecks"
})
public class VkValidationFlagsEXT extends Structure {
	public static class ByValue extends VkValidationFlagsEXT implements Structure.ByValue { }
	public static class ByReference extends VkValidationFlagsEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_VALIDATION_FLAGS_EXT.value();
	public Pointer pNext;
	public int disabledValidationCheckCount;
	public int pDisabledValidationChecks;
}
