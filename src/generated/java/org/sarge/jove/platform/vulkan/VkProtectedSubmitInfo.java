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
	"protectedSubmit"
})
public class VkProtectedSubmitInfo extends VulkanStructure {
	public static class ByValue extends VkProtectedSubmitInfo implements Structure.ByValue { }
	public static class ByReference extends VkProtectedSubmitInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PROTECTED_SUBMIT_INFO;
	public Pointer pNext;
	public boolean protectedSubmit;
}
