package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.render.FrameComposer.BufferPolicy;
import org.sarge.jove.platform.vulkan.render.SwapchainTest.MockSwapchainLibrary;

class RenderTaskTest {
	private static class MockRenderTaskLibrary extends MockSwapchainLibrary {
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
	}

	private RenderTask task;
	private LogicalDevice device;
	private MockSwapchain swapchain;
	private Framebuffer.Group group;
	private AtomicReference<Buffer> sequence;
	private FrameComposer composer;

	@BeforeEach
	void before() {
		device = new MockLogicalDevice(new MockRenderTaskLibrary());
		swapchain = new MockSwapchain(device);
		group = new Framebuffer.Group(swapchain, new MockRenderPass(device), List.of());
		sequence = new AtomicReference<>();
		composer = new FrameComposer(new MockCommandPool(), BufferPolicy.DEFAULT, sequence::set);
		task = new RenderTask(swapchain, group, composer);
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
		swapchain.invalidate();
		task.run();
		// TODO
	}

	@Test
	void destroy() {
		task.destroy();
	}
}
