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
public class VkSpecializationInfo implements NativeStructure {
	public int mapEntryCount;
	public VkSpecializationMapEntry[] pMapEntries;
	public long dataSize;
	public Handle pData;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("mapEntryCount"),
			PADDING,
			POINTER.withName("pMapEntries"),
			JAVA_LONG.withName("dataSize"),
			POINTER.withName("pData")
		);
	}
}
