package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBindImageMemoryDeviceGroupInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int deviceIndexCount;
	public int[] pDeviceIndices;
	public int splitInstanceBindRegionCount;
	public VkRect2D[] pSplitInstanceBindRegions;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("deviceIndexCount"),
			PADDING,
			POINTER.withName("pDeviceIndices"),
			JAVA_INT.withName("splitInstanceBindRegionCount"),
			PADDING,
			POINTER.withName("pSplitInstanceBindRegions")
		);
	}
}
