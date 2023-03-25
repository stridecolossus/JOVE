package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;

public class FrameBufferTest {
	private static final Rectangle EXTENTS = new Rectangle(new Dimensions(2, 3));

	private FrameBuffer buffer;
	private RenderPass pass;
	private MockImage image;
	private View view;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();
		lib = dev.library();

		// Create image view
		image = new MockImage();
		view = new View.Builder(image).build(dev);
		view.clear(new ColourClearValue(Colour.WHITE));

		// Create render pass
		final Attachment attachment = Attachment.colour(VkFormat.R32G32B32A32_SFLOAT);
		pass = new RenderPass(new Handle(2), dev, List.of(attachment));

		// Create frame buffer
		buffer = new FrameBuffer(new Handle(1), dev, pass, List.of(view), EXTENTS);
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
		// Start render pass
		final Command begin = buffer.begin(VkSubpassContents.INLINE);
		final Command.Buffer cmd = mock(Command.Buffer.class);
		begin.record(lib, cmd);

		// Check API
		final var expected = new VkRenderPassBeginInfo() {
			@Override
			public boolean equals(Object obj) {
				// Check descriptor
				final var actual = (VkRenderPassBeginInfo) obj;
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
		FrameBuffer.END.record(lib, cmd);
		verify(lib).vkCmdEndRenderPass(cmd);
	}

	@Nested
	class CreateTests {
		@DisplayName("A frame buffer can be created for a swapchain image")
		@Test
		void create() {
			// Construct buffer
			buffer = FrameBuffer.create(pass, EXTENTS, List.of(view));
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
			verify(lib).vkCreateFramebuffer(dev, expected, null, dev.factory().pointer());
		}

		@DisplayName("The number of configured attachments should match the render pass")
		@Test
		void createInvalidAttachmentCount() {
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, EXTENTS, List.of(view, view)));
		}

		@DisplayName("The frame buffer image formats should match the attachments")
		@Test
		void createInvalidFormat() {
			image.descriptor.format(VkFormat.UNDEFINED);
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, EXTENTS, List.of(view)));
		}

		@DisplayName("The attachment dimensions cannot be smaller than the frame buffer")
		@Test
		void createInvalidExtents() {
			image.descriptor.extents(new Dimensions(1, 2));
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, EXTENTS, List.of(view)));
		}
	}

	// TODO - group tests
}
