package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.present.*;

class RenderControllerTest {
	private static class MockSwapchainManager extends SwapchainManager {
		public MockSwapchainManager() {
			super(
					new MockLogicalDevice(),
					new MockSurfaceProperties(),
					new Swapchain.Builder().format(MockSurfaceProperties.FORMAT),
					List.of()
			);
		}

		int count;

		@Override
		protected Swapchain build() {
			++count;
			return new MockSwapchain();
		}
	}

	private RenderController controller;
	private MockSwapchainManager manager;
	private Framebuffer.Factory framebuffers;

	@BeforeEach
	void before() {
		manager = new MockSwapchainManager();

		framebuffers = new Framebuffer.Factory(new MockRenderPass()) {
			@Override
			protected Framebuffer create(List<View> views, Dimensions extents) {
				return new MockFramebuffer();
			}
		};

		controller = new RenderController(manager, framebuffers, new MockLogicalDevice());
	}

	@Test
	void constructor() {
		assertNotNull(framebuffers.get(0));
		// TODO - check attachments
		assertEquals(1, manager.count);
	}

	@Test
	void recreate() {
		controller.recreate();
		assertNotNull(framebuffers.get(0));
		// TODO - check attachments
		assertEquals(2, manager.count);
	}
}
