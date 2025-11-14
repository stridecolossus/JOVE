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
public class VkImageViewCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.IMAGE_VIEW_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkImageViewCreateFlag> flags;
	public Handle image;
	public VkImageViewType viewType;
	public VkFormat format;
	public VkComponentMapping components;
	public VkImageSubresourceRange subresourceRange;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
		        JAVA_INT.withName("sType"),
		        PADDING,
		        POINTER.withName("pNext"),
		        JAVA_INT.withName("flags"),
		        PADDING,
		        POINTER.withName("image"),
		        JAVA_INT.withName("viewType"),
		        JAVA_INT.withName("format"),
		        MemoryLayout.structLayout(
    	                JAVA_INT.withName("r"),
    	                JAVA_INT.withName("g"),
    	                JAVA_INT.withName("b"),
    	                JAVA_INT.withName("a")
	            ).withName("components"),
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
