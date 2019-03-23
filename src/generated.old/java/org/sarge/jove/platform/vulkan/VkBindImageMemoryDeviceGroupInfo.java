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
	"deviceIndexCount",
	"pDeviceIndices",
	"splitInstanceBindRegionCount",
	"pSplitInstanceBindRegions"
})
public class VkBindImageMemoryDeviceGroupInfo extends Structure {
	public static class ByValue extends VkBindImageMemoryDeviceGroupInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindImageMemoryDeviceGroupInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_DEVICE_GROUP_INFO.value();
	public Pointer pNext;
	public int deviceIndexCount;
	public int pDeviceIndices;
	public int splitInstanceBindRegionCount;
	public VkRect2D.ByReference pSplitInstanceBindRegions;
}
