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
	"flags",
	"pfnCallback",
	"pUserData"
})
public class VkDebugReportCallbackCreateInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public Callback pfnCallback;
	public Pointer pUserData;
}
