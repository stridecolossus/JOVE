package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

public class MockSwapchainLibrary extends MockVulkanLibrary {
	private boolean concurrent;
	boolean destroyed;
	VkResult result = VkResult.VK_SUCCESS;

	@Override
	public VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain) {
		assertNotNull(device);
		assertEquals(null, pAllocator);

		assertEquals(new EnumMask<>(), pCreateInfo.flags);
		assertNotNull(pCreateInfo.surface);
		assertEquals(1, pCreateInfo.minImageCount);
		assertEquals(VkFormat.R32G32B32_SFLOAT, pCreateInfo.imageFormat);
		assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, pCreateInfo.imageColorSpace);
		assertTrue(pCreateInfo.imageExtent.width >= 640);
		assertTrue(pCreateInfo.imageExtent.width <= 1024);
		assertTrue(pCreateInfo.imageExtent.height >= 480);
		assertTrue(pCreateInfo.imageExtent.height <= 768);
		assertEquals(1, pCreateInfo.imageArrayLayers);
		assertEquals(new EnumMask<>(VkImageUsageFlags.COLOR_ATTACHMENT), pCreateInfo.imageUsage);
		if(concurrent) {
			assertEquals(VkSharingMode.CONCURRENT, pCreateInfo.imageSharingMode);
			assertEquals(1, pCreateInfo.queueFamilyIndexCount);
		}
		else {
			assertEquals(VkSharingMode.EXCLUSIVE, pCreateInfo.imageSharingMode);
			assertEquals(0, pCreateInfo.queueFamilyIndexCount);
		}
		assertEquals(new EnumMask<>(VkSurfaceTransformFlagsKHR.IDENTITY_KHR), pCreateInfo.preTransform);
		assertEquals(new EnumMask<>(VkCompositeAlphaFlagsKHR.OPAQUE_KHR), pCreateInfo.compositeAlpha);
		assertNotNull(pCreateInfo.presentMode);
		assertEquals(true, pCreateInfo.clipped);
		assertEquals(null, pCreateInfo.oldSwapchain);

		pSwapchain.set(MemorySegment.ofAddress(2));
		return VkResult.VK_SUCCESS;
	}

	@Override
	public VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView) {
		assertNotNull(device);
		assertEquals(null, pAllocator);
		pView.set(MemorySegment.ofAddress(3));
		return VkResult.VK_SUCCESS;
	}

	@Override
	public void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Handle pAllocator) {
		assertNotNull(device);
		assertNotNull(swapchain);
		assertEquals(null, pAllocator);
		destroyed = true;
	}

	@Override
	public VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, Handle[] pSwapchainImages) {
		assertNotNull(device);
		assertNotNull(swapchain);
		if(pSwapchainImages == null) {
			pSwapchainImageCount.set(1);
		}
		else {
			pSwapchainImages[0] = new Handle(4);
		}
		return VkResult.VK_SUCCESS;
	}

	@Override
	public int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex) {
		assertNotNull(device);
		assertNotNull(swapchain);
		assertEquals(Long.MAX_VALUE, timeout);
		pImageIndex.set(0);
		return result.value();
	}

	@Override
	public int vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo) {
		return result.value();
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
		pSurfaceCapabilities.currentExtent = new VkExtent2D();
		pSurfaceCapabilities.currentExtent.width = 640;
		pSurfaceCapabilities.currentExtent.height = 480;
		pSurfaceCapabilities.supportedTransforms = new EnumMask<>(VkSurfaceTransformFlagsKHR.IDENTITY_KHR);
		pSurfaceCapabilities.currentTransform = new EnumMask<>(VkSurfaceTransformFlagsKHR.IDENTITY_KHR);
		pSurfaceCapabilities.maxImageArrayLayers = 1;
		pSurfaceCapabilities.minImageCount = 1;
		pSurfaceCapabilities.maxImageCount = 2;
		pSurfaceCapabilities.supportedUsageFlags = new EnumMask<>(VkImageUsageFlags.COLOR_ATTACHMENT);
		pSurfaceCapabilities.supportedCompositeAlpha = new EnumMask<>(VkCompositeAlphaFlagsKHR.OPAQUE_KHR);
		return VkResult.VK_SUCCESS;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkPresentModeKHR[] modes) {
		if(modes == null) {
			count.set(2);
		}
		else {
			modes[0] = VkPresentModeKHR.FIFO_KHR;
			modes[1] = VkPresentModeKHR.MAILBOX_KHR;
		}
		return VkResult.VK_SUCCESS;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkSurfaceFormatKHR[] formats) {
		if(formats == null) {
			count.set(1);
		}
		else {
			final var format = new VkSurfaceFormatKHR();
			format.format = VkFormat.B8G8R8A8_UNORM;
			format.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;
			formats[0] = format;
		}
		return VkResult.VK_SUCCESS;
	}
}
