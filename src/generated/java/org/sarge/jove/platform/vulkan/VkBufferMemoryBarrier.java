package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBufferMemoryBarrier implements NativeStructure {
	public final VkStructureType sType = VkStructureType.BUFFER_MEMORY_BARRIER;
	public Handle pNext;
	public EnumMask<VkAccess> srcAccessMask;
	public EnumMask<VkAccess> dstAccessMask;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Handle buffer;
	public long offset;
	public long size;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("srcAccessMask"),
				JAVA_INT.withName("dstAccessMask"),
				JAVA_INT.withName("srcQueueFamilyIndex"),
				JAVA_INT.withName("dstQueueFamilyIndex"),
				POINTER.withName("buffer"),
				JAVA_INT.withName("offset"),
				JAVA_INT.withName("size")
		);
	}
}
