package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageSubresourceRange implements NativeStructure {
	public EnumMask<VkImageAspect> aspectMask;
	public int baseMipLevel;
	public int levelCount;
	public int baseArrayLayer;
	public int layerCount;

	@Override
	public GroupLayout layout() {
        return MemoryLayout.structLayout(
	            JAVA_INT.withName("aspectMask"),
	            JAVA_INT.withName("baseMipLevel"),
	            JAVA_INT.withName("levelCount"),
	            JAVA_INT.withName("baseArrayLayer"),
	            JAVA_INT.withName("layerCount")
        );
	}
}
