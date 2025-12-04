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
public class VkPhysicalDeviceImageFormatInfo2 implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkFormat format;
	public VkImageType type;
	public VkImageTiling tiling;
	public EnumMask<VkImageUsageFlags> usage;
	public EnumMask<VkImageCreateFlags> flags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("format"),
			JAVA_INT.withName("type"),
			JAVA_INT.withName("tiling"),
			JAVA_INT.withName("usage"),
			JAVA_INT.withName("flags")
		);
	}
}
