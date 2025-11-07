package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.*;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBufferCopy extends VulkanStructure {
	public long srcOffset;
	public long dstOffset;
	public long size;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_LONG.withName("srcOffset"),
				JAVA_LONG.withName("dstOffset"),
				JAVA_LONG.withName("size")
		);
	}
}
