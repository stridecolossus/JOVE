package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Callback;
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
	"flags",
	"pfnCallback",
	"pUserData"
})
public class VkDebugReportCallbackCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDebugReportCallbackCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugReportCallbackCreateInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public Callback pfnCallback;
	public Pointer pUserData;
}
