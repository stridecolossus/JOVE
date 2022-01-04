package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"depthClipEnable"
})
public class VkPhysicalDeviceDepthClipEnableFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceDepthClipEnableFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDepthClipEnableFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_DEPTH_CLIP_ENABLE_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean depthClipEnable;
}
