package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.present.MockSwapchain;
import org.sarge.jove.util.EnumMask;

class FramebufferTest {
	static class MockFramebufferLibrary extends MockVulkanLibrary {
		public boolean begin;
		public boolean end;
		public boolean destroyed;

		@Override
		public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertNotNull(pCreateInfo.renderPass);
			assertNotEquals(0, pCreateInfo.attachmentCount);
			assertNotEquals(0, pCreateInfo.pAttachments.length);
			assertEquals(640, pCreateInfo.width);
			assertEquals(480, pCreateInfo.height);
			assertEquals(1, pCreateInfo.layers);
			pFramebuffer.set(MemorySegment.ofAddress(3));
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents) {
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

			begin = true;
		}

		@Override
		public void vkCmdEndRenderPass(Buffer commandBuffer) {
			end = true;
		}

		@Override
		public void vkDestroyFramebuffer(LogicalDevice device, Framebuffer framebuffer, Handle pAllocator) {
			destroyed = true;
		}
	}

	private Framebuffer framebuffer;
	private MockFramebufferLibrary library;
	private LogicalDevice device;
	private RenderPass pass;

	@BeforeEach
	void before() {
		library = new MockFramebufferLibrary();
		device = new MockLogicalDevice(library);
		pass = new MockRenderPass(device);
		framebuffer = new Framebuffer(new Handle(1), pass, new Dimensions(640, 480));
	}

	@Test
	void begin() {
		final Command begin = framebuffer.begin(VkSubpassContents.INLINE);
		begin.execute(null);
		assertEquals(true, library.begin);
	}

	@Test
	void end() {
		final Command end = framebuffer.end();
		end.execute(null);
		assertEquals(true, library.end);
	}

	@Test
	void destroy() {
		framebuffer.destroy();
		assertEquals(true, framebuffer.isDestroyed());
		assertEquals(true, library.destroyed);
	}

	@Test
	void factory() {
		final var factory = new Framebuffer.Factory(pass);
		final var swapchain = new MockSwapchain(device);
		factory.build(swapchain);
		assertNotNull(factory.framebuffer(0));
	}
}
