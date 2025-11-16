package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.render.FrameComposer.BufferPolicy;
import org.sarge.jove.platform.vulkan.render.SwapchainTest.MockSwapchainLibrary;

class RenderTaskTest {
	private static class MockRenderTaskLibrary extends MockSwapchainLibrary {
		private VkResult result = VkResult.SUCCESS;

		@Override
		public VkResult vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore) {
			pSemaphore.set(new Handle(4));
			return null;
		}

		@Override
		public VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence) {
			pFence.set(new Handle(5));
			return null;
		}

		@Override
		public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
			pFramebuffer.set(new Handle(6));
			return VkResult.SUCCESS;
		}

		@Override
		public int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex) {
			pImageIndex.set(0);
			return result.value();
		}
	}

	private RenderTask task;
	private LogicalDevice device;
	private MockRenderTaskLibrary library;
	private Framebuffer.Group group;
	private AtomicReference<Buffer> sequence;
	private FrameComposer composer;

	@BeforeEach
	void before() {
		library = new MockRenderTaskLibrary();
		device = new MockLogicalDevice(library);

		final var surface = new MockVulkanSurface(library);
		final var physical = new MockPhysicalDevice(library);
		final var factory = new SwapchainFactory(device, surface.new PropertiesAdapter(physical));

		group = new Framebuffer.Group(factory.swapchain(), new MockRenderPass(device), List.of());
		sequence = new AtomicReference<>();
		composer = new FrameComposer(new MockCommandPool(), BufferPolicy.DEFAULT, sequence::set);
		task = new RenderTask(factory, group, composer);
	}

	@Test
	void run() {
		task.run();
		// TODO
		// - swapchain acquire
		// - compose index, framebuffer
		// - render
		// - present index, swapchain
		final Buffer buffer = sequence.get();
		assertEquals(true, buffer.isPrimary());
		assertEquals(true, buffer.isReady()); // TODO - invalidated?
	}

	@Test
	void invalidated() {
		library.result = VkResult.ERROR_OUT_OF_DATE_KHR;
		task.run();
	}

	@Test
	void destroy() {
		task.destroy();
	}
}
