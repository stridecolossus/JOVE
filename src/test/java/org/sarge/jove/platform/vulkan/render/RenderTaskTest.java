package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.MemorySegment;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.present.*;

class RenderTaskTest {
	private static class MockRenderTaskLibrary extends MockSwapchainLibrary {
		private VkResult result = VkResult.VK_SUCCESS;

		@Override
		public VkResult vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore) {
			pSemaphore.set(MemorySegment.ofAddress(4));
			return null;
		}

		@Override
		public VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence) {
			pFence.set(MemorySegment.ofAddress(5));
			return null;
		}

		@Override
		public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
			pFramebuffer.set(MemorySegment.ofAddress(6));
			return VkResult.VK_SUCCESS;
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
	private RenderSequence sequence;
	private FrameComposer composer;

	// TODO - clean
	@BeforeEach
	void before() {
		library = new MockRenderTaskLibrary();
		device = new MockLogicalDevice(library);

		final var properties = new MockSurfaceProperties();

		final var builder = new Swapchain.Builder()
				.count(1)
				.format(MockSurfaceProperties.FORMAT)
				.extent(new Dimensions(640, 480));

		final var manager = new SwapchainManager(device, properties, builder, List.of());

		sequence = new RenderSequence() {
			@Override
			public void build(int index, Buffer buffer) {
				assertEquals(0, index);
				assertEquals(true, buffer.isPrimary());
				assertEquals(true, buffer.isReady());
			}
		};

		final var factory = new Framebuffer.Factory(new MockRenderPass(device));

		composer = new FrameComposer(new MockCommandPool(), sequence);

		task = new RenderTask(manager, factory::create, composer);
	}

	// TODO
	@Test
	void run() {
		task.run();
		// TODO
		// - swapchain acquire
		// - compose index, framebuffer
		// - render
		// - present index, swapchain
//		final Buffer buffer = sequence.get();
//		assertEquals(true, buffer.isPrimary());
//		assertEquals(true, buffer.isReady()); // TODO - invalidated?
	}

	@Test
	void invalidated() {
		library.result = VkResult.VK_ERROR_OUT_OF_DATE_KHR;
		task.run();
	}

	@Test
	void destroy() {
		task.destroy();
	}
}
