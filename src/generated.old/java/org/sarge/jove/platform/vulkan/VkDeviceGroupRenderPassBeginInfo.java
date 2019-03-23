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
	"deviceMask",
	"deviceRenderAreaCount",
	"pDeviceRenderAreas"
})
public class VkDeviceGroupRenderPassBeginInfo extends Structure {
	public static class ByValue extends VkDeviceGroupRenderPassBeginInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupRenderPassBeginInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_RENDER_PASS_BEGIN_INFO.value();
	public Pointer pNext;
	public int deviceMask;
	public int deviceRenderAreaCount;
	public VkRect2D.ByReference pDeviceRenderAreas;
}
