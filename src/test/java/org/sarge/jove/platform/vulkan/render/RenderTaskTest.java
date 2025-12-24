package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.present.*;
import org.sarge.jove.platform.vulkan.present.VulkanSurface.Properties;

class RenderTaskTest {
	private RenderTask task;

	@BeforeEach
	void before() {
		final var device = new MockLogicalDevice();

		final var builder = new Swapchain.Builder() {
			@Override
			public Swapchain build(LogicalDevice device, Properties properties) {
				return new MockSwapchain();
			}
		};

		final var manager = new SwapchainManager(device, MockSurfaceProperties::new, builder, List.of());

		final var framebuffers = new Framebuffer.Factory(new MockRenderPass()) {
			@Override
			protected Framebuffer create(List<View> views, Dimensions extents) {
				return new MockFramebuffer();
			}
		};

		final var sequence = new RenderSequence() {
			@Override
			public void build(int index, Buffer buffer) {
				// Empty
			}
		};

		final var composer = new FrameComposer(new MockCommandPool(), sequence);

		final var iterator = new FrameIterator(device, 2) {
			@Override
			protected FrameState create(int index, LogicalDevice device) {
				return new FrameState(index, new MockVulkanSemaphore(), new MockVulkanSemaphore(), new MockFence());
			}
		};

		task = new RenderTask(manager, framebuffers, composer, iterator);
	}

	@Test
	void run() {
		task.run();
	}

	@Test
	void invalidated() {

	}
}
