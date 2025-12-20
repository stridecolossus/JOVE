package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.present.*;

class RenderTaskTest {
	private RenderTask task;
	private MockSwapchain swapchain;
	private boolean acquired, rendered, presented;

	@BeforeEach
	void before() {
		// Init swapchain
		swapchain = new MockSwapchain() {
			@Override
			public int acquire(VulkanSemaphore semaphore, Fence fence) throws Invalidated {
				acquired = true;
				return 1;
			}

			@Override
			public void present(WorkQueue queue, int index, Set<VulkanSemaphore> semaphores) throws Invalidated {
				super.present(queue, index, semaphores);
				assertEquals(1, index);
				presented = true;
			}
		};
		final var builder = new Swapchain.Builder() {
			@Override
			public Swapchain build(LogicalDevice device, Properties properties) {
				return swapchain;
			}
		};
		final var device = new MockLogicalDevice();
		final var manager = new SwapchainManager(device, new MockSurfaceProperties(), builder, List.of()) {
			@Override
			protected View view(Image image) {
				return new MockView();
			}
		};

		// Init framebuffers
		final var factory = new Framebuffer.Factory(new MockRenderPass()) {
			@Override
			public void build(Swapchain swapchain) {
				// Ignored
			}

			@Override
			public Framebuffer framebuffer(int index) {
				return new MockFramebuffer();
			}
		};

		// Init composer
		final var sequence = new RenderSequence() {
			@Override
			public void build(int index, Buffer buffer) {
				assertEquals(0, index);
				rendered = true;
			}
		};
		final var composer = new FrameComposer(new MockCommandPool(), sequence);

		// Init flags
		acquired = false;
		rendered = false;
		presented = false;

		// Create task
		task = new RenderTask(manager, factory, composer) {
			@Override
			protected FrameState frame(LogicalDevice device) {
				return new FrameState(new MockVulkanSemaphore(), new MockVulkanSemaphore(), new MockFence());
			}
		};
	}

	@Test
	void constructor() {
		assertFalse(task.isDestroyed());
	}

	@Test
	void run() {
		task.run();
		assertEquals(true, acquired);
		assertEquals(true, rendered);
		assertEquals(true, presented);
	}

	@Test
	void invalidated() {
		swapchain.invalid = true;
		task.run();
		assertEquals(true, acquired);
		assertEquals(true, rendered);
		assertEquals(false, presented);
	}

	@Test
	void destroy() {
		task.destroy();
	}
}
