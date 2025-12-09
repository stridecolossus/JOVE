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
public class VkSurfaceCapabilities2EXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int minImageCount;
	public int maxImageCount;
	public VkExtent2D currentExtent;
	public VkExtent2D minImageExtent;
	public VkExtent2D maxImageExtent;
	public int maxImageArrayLayers;
	public EnumMask<VkSurfaceTransformFlagsKHR> supportedTransforms;
	public EnumMask<VkSurfaceTransformFlagsKHR> currentTransform;
	public EnumMask<VkCompositeAlphaFlagsKHR> supportedCompositeAlpha;
	public EnumMask<VkImageUsageFlags> supportedUsageFlags;
	public EnumMask<VkSurfaceCounterFlagsEXT> supportedSurfaceCounters;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("minImageCount"),
			JAVA_INT.withName("maxImageCount"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("currentExtent"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("minImageExtent"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("maxImageExtent"),
			JAVA_INT.withName("maxImageArrayLayers"),
			JAVA_INT.withName("supportedTransforms"),
			JAVA_INT.withName("currentTransform"),
			JAVA_INT.withName("supportedCompositeAlpha"),
			JAVA_INT.withName("supportedUsageFlags"),
			JAVA_INT.withName("supportedSurfaceCounters")
		);
	}
}
