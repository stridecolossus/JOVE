package org.sarge.jove.platform.vulkan.present;

import java.util.List;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

public class MockSurfaceProperties implements VulkanSurface.Properties {
	public static final VkSurfaceFormatKHR FORMAT = new SurfaceFormatWrapper(VkFormat.B8G8R8A8_UNORM, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);

	public VkSurfaceCapabilitiesKHR capabilities = new VkSurfaceCapabilitiesKHR();

	public MockSurfaceProperties() {
		capabilities.minImageCount = 1;
		capabilities.maxImageCount = 2;
		capabilities.currentExtent = extent(800, 600);
		capabilities.minImageExtent = extent(640, 480);
		capabilities.maxImageExtent = extent(1024, 768);
		capabilities.maxImageArrayLayers = 1;
		capabilities.currentTransform = new EnumMask<>(VkSurfaceTransformFlagsKHR.IDENTITY_KHR);
		capabilities.supportedUsageFlags = new EnumMask<>(VkImageUsageFlags.COLOR_ATTACHMENT);
		capabilities.supportedTransforms = new EnumMask<>(VkSurfaceTransformFlagsKHR.IDENTITY_KHR);
		capabilities.supportedCompositeAlpha = new EnumMask<>(VkCompositeAlphaFlagsKHR.OPAQUE_KHR);
	}

	private static VkExtent2D extent(int width, int height) {
		final var extent = new VkExtent2D();
		extent.width = width;
		extent.height = height;
		return extent;
	}

	@Override
	public VulkanSurface surface() {
		return new MockVulkanSurface();
	}

	@Override
	public VkSurfaceCapabilitiesKHR capabilities() {
		return capabilities;
	}

	@Override
	public List<VkSurfaceFormatKHR> formats() {
		return List.of(FORMAT);
	}

	@Override
	public List<VkPresentModeKHR> modes() {
		return List.of(VkPresentModeKHR.FIFO_KHR);
	}
}
