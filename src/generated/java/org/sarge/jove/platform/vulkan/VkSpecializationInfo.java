package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSpecializationInfo implements NativeStructure {
	public int mapEntryCount;
	public VkSpecializationMapEntry[] pMapEntries;
	public long dataSize;
	public byte[] pData;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("mapEntryCount"),
				PADDING,
				POINTER.withName("pMapEntries"),
				JAVA_INT.withName("dataSize"),
				PADDING,
				POINTER.withName("pData")
		);
	}
}
