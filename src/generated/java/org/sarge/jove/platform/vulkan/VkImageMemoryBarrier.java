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
public class VkImageMemoryBarrier implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkAccessFlags> srcAccessMask;
	public EnumMask<VkAccessFlags> dstAccessMask;
	public VkImageLayout oldLayout;
	public VkImageLayout newLayout;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Handle image;
	public VkImageSubresourceRange subresourceRange;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("srcAccessMask"),
			JAVA_INT.withName("dstAccessMask"),
			JAVA_INT.withName("oldLayout"),
			JAVA_INT.withName("newLayout"),
			JAVA_INT.withName("srcQueueFamilyIndex"),
			JAVA_INT.withName("dstQueueFamilyIndex"),
			POINTER.withName("image"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("baseMipLevel"),
				JAVA_INT.withName("levelCount"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
			).withName("subresourceRange"),
			PADDING
		);
	}
}
