package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBufferCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.BUFFER_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public long size;
	public EnumMask<VkBufferUsageFlag> usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public int[] pQueueFamilyIndices;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				PADDING,
				JAVA_LONG.withName("size"),
				JAVA_INT.withName("usage"),
				JAVA_INT.withName("sharingMode"),
				JAVA_INT.withName("queueFamilyIndexCount"),
				PADDING,
				POINTER.withName("pQueueFamilyIndices")
		);
	}
}
