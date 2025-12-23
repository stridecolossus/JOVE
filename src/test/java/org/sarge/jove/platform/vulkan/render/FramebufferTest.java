package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.present.MockSwapchain;
import org.sarge.jove.util.*;

class FramebufferTest {
	@SuppressWarnings("unused")
	private static class MockFramebufferLibrary extends MockLibrary {
		public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
			assertEquals(VkStructureType.FRAMEBUFFER_CREATE_INFO, pCreateInfo.sType);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertNotNull(pCreateInfo.renderPass);
			assertNotEquals(0, pCreateInfo.attachmentCount);
			assertNotEquals(0, pCreateInfo.pAttachments.length);
			assertEquals(640, pCreateInfo.width);
			assertEquals(480, pCreateInfo.height);
			assertEquals(1, pCreateInfo.layers);
			init(pFramebuffer);
			return VkResult.VK_SUCCESS;
		}

		public void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents) {
			assertEquals(VkStructureType.RENDER_PASS_BEGIN_INFO, pRenderPassBegin.sType);
			assertNotNull(pRenderPassBegin.renderPass);
			assertNotNull(pRenderPassBegin.framebuffer);
			assertEquals(0, pRenderPassBegin.renderArea.offset.x);
			assertEquals(0, pRenderPassBegin.renderArea.offset.y);
			assertEquals(640, pRenderPassBegin.renderArea.extent.width);
			assertEquals(480, pRenderPassBegin.renderArea.extent.height);
			assertEquals(1, pRenderPassBegin.clearValueCount);
			assertEquals(1, pRenderPassBegin.pClearValues.length);

			final var colour = pRenderPassBegin.pClearValues[0].color;
			if(colour != null) {
				assertArrayEquals(new float[]{0, 0, 0, 1}, colour.float32);
			}
		}
	}

	private Framebuffer framebuffer;
	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockFramebufferLibrary(), Framebuffer.Library.class, RenderPass.Library.class);
		device = new MockLogicalDevice(mockery.proxy());
		framebuffer = new Framebuffer(new Handle(1), device, new MockRenderPass(), new Dimensions(640, 480));
	}

	@Test
	void constructor() {
		assertFalse(framebuffer.isDestroyed());
	}

	@Test
	void begin() {
		final Command begin = framebuffer.begin(VkSubpassContents.INLINE);
		begin.execute(null);
		assertEquals(1, mockery.mock("vkCmdBeginRenderPass").count());
	}

	@Test
	void end() {
		final Command end = framebuffer.end();
		end.execute(null);
		assertEquals(1, mockery.mock("vkCmdEndRenderPass").count());
	}

	@Test
	void destroy() {
		framebuffer.destroy();
		assertTrue(framebuffer.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyFramebuffer").count());
	}

	@Test
	void factory() {
		final var pass = new MockRenderPass() {
			@Override
			public LogicalDevice device() {
				return device;
			}
		};
		final var factory = new Framebuffer.Factory(pass);
		pass.attachments().forEach(attachment -> attachment.recreate(device, new Dimensions(640, 480)));
		factory.recreate(new MockSwapchain());
		assertEquals(1, mockery.mock("vkCreateFramebuffer").count());
		assertNotNull(factory.get(0));
	}
}
