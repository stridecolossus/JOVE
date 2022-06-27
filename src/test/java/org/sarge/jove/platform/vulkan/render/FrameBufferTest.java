package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.*;
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

	@Test
	void constructor() {
		assertEquals(new Handle(1), buffer.handle());
		assertEquals(false, buffer.isDestroyed());
		assertEquals(List.of(view), buffer.attachments());
	}

	@Test
	void destroy() {
		buffer.destroy();
		assertEquals(true, buffer.isDestroyed());
		verify(lib).vkDestroyFramebuffer(dev, buffer, null);
	}

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

	@Test
	void end() {
		final Command.Buffer cmd = mock(Command.Buffer.class);
		FrameBuffer.END.execute(lib, cmd);
		verify(lib).vkCmdEndRenderPass(cmd);
	}

	@Nested
	class BuilderTest {
		private FrameBuffer.Builder builder;

		@BeforeEach
		void before() {
			builder = new FrameBuffer.Builder()
					.pass(pass)
					.extents(EXTENTS)
					.attachment(view);
		}

		private void init(VkFormat format, Dimensions extents) {
			// Init image
			final ImageDescriptor descriptor = new ImageDescriptor.Builder()
					.format(format)
					.extents(new ImageData.Extents(extents))
					.aspect(VkImageAspect.COLOR)
					.build();

			// Create image
			final Image image = mock(Image.class);
			when(view.image()).thenReturn(image);
			when(image.descriptor()).thenReturn(descriptor);
		}

		@Test
		void build() {
			// Construct buffer
			init(FORMAT, EXTENTS);
			assertNotNull(builder.build());

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
			verify(lib).vkCreateFramebuffer(dev, expected, null, POINTER);
		}

		@DisplayName("Number of configured attachments should match the render pass")
		@Test
		void buildInvalidAttachmentCount() {
			init(FORMAT, EXTENTS);
			builder.attachment(view);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("Frame buffer image formats should match the attachments")
		@Test
		void buildInvalidAttachmentFormat() {
			init(VkFormat.UNDEFINED, EXTENTS);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("Attachment dimensions cannot be smaller than the frame buffer")
		@Test
		void buildInvalidAttachmentExtents() {
			init(FORMAT, new Dimensions(1, 2));
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildMultiple() {
			// Init attachment
			init(FORMAT, EXTENTS);

			// Build buffers
			final List<FrameBuffer> list = new FrameBuffer.Builder()
					.pass(pass)
					.extents(EXTENTS)
					.build(List.of(view));

			// Check results
			assertNotNull(list);
			assertEquals(1, list.size());

			// Check frame buffer
			buffer = list.get(0);
			assertNotNull(buffer);
			assertEquals(List.of(view), buffer.attachments());
		}
	}
}
