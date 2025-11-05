package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.CommandBuffer;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.Swapchain;

public class MockVulkanLibrary implements Instance.Library, PhysicalDevice.Library, LogicalDevice.Library, Swapchain.Library, View.Library {
	// Instance

	@Override
	public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
		return null;
	}

	@Override
	public void vkDestroyInstance(Instance instance, Handle pAllocator) {
	}

	@Override
	public VkResult vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, VkExtensionProperties[] pProperties) {
		return null;
	}

	@Override
	public VkResult vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, VkLayerProperties[] pProperties) {
		return null;
	}

	@Override
	public Handle vkGetInstanceProcAddr(Instance instance, String pName) {
		return null;
	}

	// Physical Device

	@Override
	public VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, Handle[] devices) {
		return null;
	}

	@Override
	public void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props) {
	}

	@Override
	public void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties) {
	}

	@Override
	public void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features) {
	}

	@Override
	public void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, VkQueueFamilyProperties[] pQueueFamilyProperties) {
	}

	@Override
	public VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, VkExtensionProperties[] extensions) {
		return null;
	}

	@Override
	public VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, VkLayerProperties[] layers) {
		return null;
	}

	@Override
	public void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props) {
	}

	// Logical Device

	@Override
	public VkResult vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, Pointer device) {
		return null;
	}

	@Override
	public void vkDestroyDevice(LogicalDevice device, Handle pAllocator) {
	}

	@Override
	public VkResult vkDeviceWaitIdle(LogicalDevice device) {
		return null;
	}

	@Override
	public void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue) {
	}

	@Override
	public VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence) {
		return null;
	}

	@Override
	public VkResult vkQueueWaitIdle(WorkQueue queue) {
		return null;
	}

	// Swapchain

	@Override
	public VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain) {
		return null;
	}

	@Override
	public void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Handle pAllocator) {
	}

	@Override
	public VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, Handle[] pSwapchainImages) {
		return null;
	}

	@Override
	public VkResult vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence,
			IntegerReference pImageIndex) {
		return null;
	}

	@Override
	public VkResult vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo) {
		return null;
	}

	// View

    @Override
    public VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView) {
    	return null;
    }

    @Override
    public void vkDestroyImageView(LogicalDevice device, View imageView, Handle pAllocator) {
    }

    @Override
    public void vkCmdClearColorImage(CommandBuffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange[] pRanges) {
    }

    @Override
    public void vkCmdClearDepthStencilImage(CommandBuffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange[] pRanges) {
    }
}
