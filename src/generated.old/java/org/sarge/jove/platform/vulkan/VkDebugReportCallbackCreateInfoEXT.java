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
	"flags",
	"pfnCallback",
	"pUserData"
})
public class VkDebugReportCallbackCreateInfoEXT extends Structure {
	public static class ByValue extends VkDebugReportCallbackCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugReportCallbackCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int flags;
	public PFN_vkDebugReportCallbackEXT pfnCallback;
	public Pointer pUserData;
}
