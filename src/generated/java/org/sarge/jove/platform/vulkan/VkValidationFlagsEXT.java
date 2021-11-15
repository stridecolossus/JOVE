package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

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
public class VkValidationFlagsEXT extends VulkanStructure {
	public static class ByValue extends VkValidationFlagsEXT implements Structure.ByValue { }
	public static class ByReference extends VkValidationFlagsEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VALIDATION_FLAGS_EXT;
	public Pointer pNext;
	public int disabledValidationCheckCount;
	public Pointer pDisabledValidationChecks;
}
