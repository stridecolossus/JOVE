package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSurfaceCapabilitiesKHR implements NativeStructure {
	public int minImageCount;
	public int maxImageCount;
	public VkExtent2D currentExtent;
	public VkExtent2D minImageExtent;
	public VkExtent2D maxImageExtent;
	public int maxImageArrayLayers;
	public EnumMask<VkSurfaceTransformFlagKHR> supportedTransforms;
	public VkSurfaceTransformFlagKHR currentTransform;
	public EnumMask<VkCompositeAlphaFlagKHR> supportedCompositeAlpha;
	public EnumMask<VkImageUsageFlag> supportedUsageFlags;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
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
	            JAVA_INT.withName("supportedUsageFlags")
	        );
	}
}
