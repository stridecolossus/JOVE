package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.render.Framebuffer.Group;

class FramebufferTest {
	private static class MockFrameBufferLibrary extends MockVulkanLibrary {
		public boolean begin;
		public boolean end;
		public boolean destroyed;

		@Override
		public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
			assertEquals(0, pCreateInfo.flags);
			assertNotNull(pCreateInfo.renderPass);
			assertNotEquals(0, pCreateInfo.attachmentCount);
			assertNotEquals(0, pCreateInfo.pAttachments.length);
			assertEquals(640, pCreateInfo.width);
			assertEquals(480, pCreateInfo.height);
			assertEquals(1, pCreateInfo.layers);
			pFramebuffer.set(MemorySegment.ofAddress(3));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents) {
			assertNotNull(pRenderPassBegin.renderPass);
			assertEquals(new Handle(3), pRenderPassBegin.framebuffer);
			assertEquals(0, pRenderPassBegin.renderArea.offset.x);
			assertEquals(0, pRenderPassBegin.renderArea.offset.y);
			assertEquals(640, pRenderPassBegin.renderArea.extent.width);
			assertEquals(480, pRenderPassBegin.renderArea.extent.height);
			assertEquals(1, pRenderPassBegin.clearValueCount);
			assertEquals(1, pRenderPassBegin.pClearValues.length);
			assertArrayEquals(new float[]{0, 0, 0, 1}, pRenderPassBegin.pClearValues[0].color.float32);
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
	private LogicalDevice device;
	private MockFrameBufferLibrary library;

	@BeforeEach
	void before() {
		library = new MockFrameBufferLibrary();
		device = new MockLogicalDevice(library);

		final var pass = new MockRenderPass(device);

		final var view = new View(new Handle(2), device, new MockImage(), false);
		view.clear(new ColourClearValue(Colour.BLACK));

		framebuffer = Framebuffer.create(pass, new Rectangle(640, 480), List.of(view));
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

	@DisplayName("A frame buffer group...")
	@Nested
	class GroupTest {
		private Group group;
		private Swapchain swapchain;
		private View depth;

		@BeforeEach
		void before() {
			final var pass = new MockRenderPass(device);
			final var colour = new View(new Handle(4), device, new MockImage(), false);
			depth = new View(new Handle(5), device, new MockImage(), true);
			swapchain = new Swapchain(new Handle(6), device, library, VkFormat.R32G32B32A32_SFLOAT, new Dimensions(640, 480), List.of(colour, colour));
			group = new Group(swapchain, pass, List.of(depth));
		}

		@DisplayName("has a number of buffers equal to the number of swapchain attachments")
		@Test
		void size() {
			assertEquals(2, group.size());
		}

		@DisplayName("creates a frame buffer for each swapchain image and any additional attachments")
		@Test
		void get() {
			for(int n = 0; n < 2; ++n) {
    			final Framebuffer buffer = group.get(0);
    			final View colour = swapchain.attachments().get(n);
    			assertEquals(false, buffer.isDestroyed());
    			assertEquals(List.of(colour, depth), buffer.attachments());
			}
		}

		@DisplayName("can be recreated if the swapchain is invalidated")
		@Test
		void create() {
			group.recreate(swapchain);
			assertEquals(2, group.size());
		}

		@DisplayName("can be destroyed releasing all buffers")
		@Test
		void destroy() {
			final Framebuffer buffer = group.get(0);
			group.destroy();
			assertEquals(0, group.size());
			assertEquals(true, buffer.isDestroyed());
			assertThrows(IndexOutOfBoundsException.class, () -> group.get(0));
		}
	}
}
