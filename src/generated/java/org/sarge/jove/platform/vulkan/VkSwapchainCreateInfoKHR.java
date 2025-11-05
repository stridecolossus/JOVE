package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSwapchainCreateInfoKHR implements NativeStructure {
	public final VkStructureType sType = VkStructureType.SWAPCHAIN_CREATE_INFO_KHR;
	public Handle pNext;
	public EnumMask<VkSwapchainCreateFlagKHR> flags;
	public Handle surface;
	public int minImageCount;
	public VkFormat imageFormat;
	public VkColorSpaceKHR imageColorSpace;
	public VkExtent2D imageExtent;
	public int imageArrayLayers;
	public EnumMask<VkImageUsageFlag> imageUsage;
	public VkSharingMode imageSharingMode;
	public int queueFamilyIndexCount;
	public int[] pQueueFamilyIndices;
	public VkSurfaceTransformFlagKHR preTransform;
	public VkCompositeAlphaFlagKHR compositeAlpha;
	public VkPresentModeKHR presentMode;
	public boolean clipped;
	public Swapchain oldSwapchain;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
	            JAVA_INT.withName("sType"),
	            PADDING,
	            POINTER.withName("pNext"),
	            JAVA_INT.withName("flags"),
	            PADDING,
	            POINTER.withName("surface"),
	            JAVA_INT.withName("minImageCount"),
	            JAVA_INT.withName("imageFormat"),
	            JAVA_INT.withName("imageColorSpace"),
	            MemoryLayout.structLayout(
	                JAVA_INT.withName("width"),
	                JAVA_INT.withName("height")
	            ).withName("imageExtent"),
	            JAVA_INT.withName("imageArrayLayers"),
	            JAVA_INT.withName("imageUsage"),
	            JAVA_INT.withName("imageSharingMode"),
	            JAVA_INT.withName("queueFamilyIndexCount"),
	            PADDING,
	            POINTER.withName("pQueueFamilyIndices"),
	            JAVA_INT.withName("preTransform"),
	            JAVA_INT.withName("compositeAlpha"),
	            JAVA_INT.withName("presentMode"),
	            JAVA_INT.withName("clipped"),
	            POINTER.withName("oldSwapchain")
		);
	}
}
