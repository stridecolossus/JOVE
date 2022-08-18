package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.render.FrameBuffer.Group;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class FrameBufferTest extends AbstractVulkanTest {
	private static final Dimensions EXTENTS = new Dimensions(2, 3);

	private FrameBuffer buffer;
	private RenderPass pass;
	private View view;

	@BeforeEach
	void before() {
		// Create image view
		final ClearValue clear = new ClearValue.ColourClearValue(Colour.WHITE);
		view = mock(View.class);
		when(view.handle()).thenReturn(new Handle(1));
		when(view.clear()).thenReturn(Optional.of(clear));

		// Create attachment
		final Attachment attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();

		// Create render pass
		pass = mock(RenderPass.class);
		when(pass.handle()).thenReturn(new Handle(2));
		when(pass.attachments()).thenReturn(List.of(attachment));
		when(pass.device()).thenReturn(dev);

		// Create frame buffer
		buffer = new FrameBuffer(new Pointer(1), dev, pass, List.of(view), EXTENTS);
	}

	@DisplayName("A frame buffer is comprised of the swapchain image and additional attachments")
	@Test
	void constructor() {
		assertEquals(new Handle(1), buffer.handle());
		assertEquals(false, buffer.isDestroyed());
		assertEquals(List.of(view), buffer.attachments());
	}

	@DisplayName("A frame buffer can be destroyed")
	@Test
	void destroy() {
		buffer.destroy();
		assertEquals(true, buffer.isDestroyed());
		verify(lib).vkDestroyFramebuffer(dev, buffer, null);
	}

	@DisplayName("A frame buffer can be used to start a render pass")
	@Test
	void begin() {
		// Create command
		final Command begin = buffer.begin();
		assertNotNull(begin);

		// Start render pass
		final Command.Buffer cmd = mock(Command.Buffer.class);
		begin.execute(lib, cmd);

		// Check API
		final var expected = new VkRenderPassBeginInfo() {
			@Override
			public boolean equals(Object obj) {
				// Check descriptor
				final var actual = (VkRenderPassBeginInfo) obj;
				assertNotNull(actual);
				assertEquals(buffer.handle(), actual.framebuffer);
				assertEquals(pass.handle(), actual.renderPass);

				// Check extents
				assertEquals(2, actual.renderArea.extent.width);
				assertEquals(3, actual.renderArea.extent.height);
				assertEquals(0, actual.renderArea.offset.x);
				assertEquals(0, actual.renderArea.offset.y);

				// Check colour clear
				assertEquals(1, actual.clearValueCount);
				assertNotNull(actual.pClearValues);
				assertNotNull(actual.pClearValues.color);
				assertArrayEquals(Colour.WHITE.toArray(), actual.pClearValues.color.float32);

				// Check depth clear
				assertNotNull(actual.pClearValues.depthStencil);
				assertEquals(0, actual.pClearValues.depthStencil.depth);
				assertEquals(0, actual.pClearValues.depthStencil.stencil);

				return true;
			}
		};
		verify(lib).vkCmdBeginRenderPass(cmd, expected, VkSubpassContents.INLINE);
	}

	@DisplayName("A frame buffer can be created to complete the render pass")
	@Test
	void end() {
		final Command.Buffer cmd = mock(Command.Buffer.class);
		FrameBuffer.END.execute(lib, cmd);
		verify(lib).vkCmdEndRenderPass(cmd);
	}

	private void init(VkFormat format, Dimensions extents) {
		// Init image
		final Descriptor descriptor = new Descriptor.Builder()
				.format(format)
				.extents(extents)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		final Image image = mock(Image.class);
		when(view.image()).thenReturn(image);
		when(image.descriptor()).thenReturn(descriptor);
	}

	@Nested
	class CreateTests {
		@BeforeEach
		void before() {
			init(FORMAT, EXTENTS);
		}

		@DisplayName("A frame buffer can be created for a swapchain image")
		@Test
		void create() {
			// Construct buffer
			buffer = FrameBuffer.create(pass, EXTENTS, List.of(view));
			assertNotNull(buffer);
			assertEquals(dev, buffer.device());
			assertEquals(false, buffer.isDestroyed());
			assertEquals(List.of(view), buffer.attachments());

			// Check API
			final var expected = new VkFramebufferCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check create descriptor
					final var actual = (VkFramebufferCreateInfo) obj;
					assertNotNull(actual);
					assertEquals(pass.handle(), actual.renderPass);
					assertEquals(0, actual.flags);

					// Check attachments
					assertEquals(1, actual.attachmentCount);
					assertEquals(NativeObject.array(List.of(view)), actual.pAttachments);

					// Check extents
					assertEquals(2, actual.width);
					assertEquals(3, actual.height);
					assertEquals(1, actual.layers);

					return true;
				}
			};
			verify(lib).vkCreateFramebuffer(dev, expected, null, factory.pointer());
		}

		@DisplayName("Number of configured attachments should match the render pass")
		@Test
		void createInvalidAttachmentCount() {
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, EXTENTS, List.of(view, view)));
		}

		@DisplayName("Frame buffer image formats should match the attachments")
		@Test
		void createInvalidFormat() {
			init(VkFormat.UNDEFINED, EXTENTS);
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, EXTENTS, List.of(view, view)));
		}

		@DisplayName("Attachment dimensions cannot be smaller than the frame buffer")
		@Test
		void createInvalidExtents() {
			init(FORMAT, new Dimensions(1, 2));
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, EXTENTS, List.of(view)));
		}
	}

	@Nested
	class FrameSetTests {
		private Group group;
		private Swapchain swapchain;

		@BeforeEach
		void before() {
			init(FORMAT, EXTENTS);
			swapchain = mock(Swapchain.class);
			when(swapchain.extents()).thenReturn(EXTENTS);
			when(swapchain.attachments()).thenReturn(List.of(view));
			group = new Group(swapchain, pass, List.of());
		}

		@Test
		void constructor() {
			assertEquals(swapchain, group.swapchain());
			assertEquals(false, group.isDestroyed());
		}

		@Test
		void buffer() {
			assertNotNull(group.buffer(0));
		}

		@Test
		void destroy() {
			group.destroy();
		}
	}
}
