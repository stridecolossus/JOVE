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
	"deviceMask",
	"deviceRenderAreaCount",
	"pDeviceRenderAreas"
})
public class VkDeviceGroupRenderPassBeginInfo extends VulkanStructure {
	public static class ByValue extends VkDeviceGroupRenderPassBeginInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupRenderPassBeginInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEVICE_GROUP_RENDER_PASS_BEGIN_INFO;
	public Pointer pNext;
	public int deviceMask;
	public int deviceRenderAreaCount;
	public Pointer pDeviceRenderAreas;
}
