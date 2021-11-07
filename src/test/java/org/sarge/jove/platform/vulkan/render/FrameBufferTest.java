package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkRenderPassBeginInfo;
import org.sarge.jove.platform.vulkan.VkSubpassContents;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor;
import org.sarge.jove.platform.vulkan.image.ImageExtents;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class FrameBufferTest extends AbstractVulkanTest {
	private FrameBuffer buffer;
	private View view;
	private RenderPass pass;
	private Attachment attachment;
	private Dimensions extents;

	@BeforeEach
	void before() {
		// Create attachment
		attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();

		// Create render pass
		pass = mock(RenderPass.class);
		when(pass.handle()).thenReturn(new Handle(new Pointer(1)));
		when(pass.device()).thenReturn(dev);
		when(pass.attachments()).thenReturn(List.of(attachment));

		// Init image descriptor
		extents = new Dimensions(3, 4);
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.extents(new ImageExtents(extents))
				.format(FORMAT)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		final Image image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create swapchain view
		view = mock(View.class);
		when(view.handle()).thenReturn(new Handle(new Pointer(2)));
		when(view.image()).thenReturn(image);
		when(view.clear()).thenReturn(new ColourClearValue(Colour.WHITE));

		// Create buffer
		buffer = FrameBuffer.create(pass, extents, List.of(view));
	}

	@Test
	void constructor() {
		assertNotNull(buffer);
		assertNotNull(buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(false, buffer.isDestroyed());
		assertEquals(List.of(view), buffer.attachments());
	}

	@Test
	void begin() {
		// Create command
		final Command begin = buffer.begin();
		assertNotNull(begin);

		// Start rendering
		final Command.Buffer cb = mock(Command.Buffer.class);
		begin.execute(lib, cb);

		// Check API
		final ArgumentCaptor<VkRenderPassBeginInfo> captor = ArgumentCaptor.forClass(VkRenderPassBeginInfo.class);
		verify(lib).vkCmdBeginRenderPass(eq(cb), captor.capture(), eq(VkSubpassContents.INLINE));

		// Check descriptor
		final VkRenderPassBeginInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(buffer.handle(), info.framebuffer);
		assertEquals(pass.handle(), info.renderPass);
		assertEquals(3, info.renderArea.extent.width);
		assertEquals(4, info.renderArea.extent.height);
		assertEquals(0, info.renderArea.offset.x);
		assertEquals(0, info.renderArea.offset.y);
		assertEquals(1, info.clearValueCount);

		// Check colour clear
		assertNotNull(info.pClearValues);
		assertNotNull(info.pClearValues.color);
		assertArrayEquals(Colour.WHITE.toArray(), info.pClearValues.color.float32);

		// Check depth clear
		assertNotNull(info.pClearValues.depthStencil);
		assertEquals(0, info.pClearValues.depthStencil.depth);
		assertEquals(0, info.pClearValues.depthStencil.stencil);
	}

	@Test
	void end() {
		final Command.Buffer cb = mock(Command.Buffer.class);
		FrameBuffer.END.execute(lib, cb);
		verify(lib).vkCmdEndRenderPass(cb);
	}

	@Test
	void destructor() {
		assertNotNull(buffer.destructor(lib));
	}

	@Test
	void destroy() {
		buffer.destroy();
		verify(lib).vkDestroyFramebuffer(dev, buffer, null);
	}

	@Nested
	class CreateTests {
		@Test
		void create() {
			// Check allocation
			final ArgumentCaptor<VkFramebufferCreateInfo> captor = ArgumentCaptor.forClass(VkFramebufferCreateInfo.class);
			verify(lib).vkCreateFramebuffer(eq(dev), captor.capture(), isNull(), eq(POINTER));

			// Check descriptor
			final VkFramebufferCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(pass.handle(), info.renderPass);
			assertEquals(1, info.attachmentCount);
			assertNotNull(info.pAttachments);
			assertEquals(3, info.width);
			assertEquals(4, info.height);
			assertEquals(1, info.layers);
			assertEquals(0, info.flags);
		}

		@Test
		void createInvalidAttachmentCount() {
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, extents, List.of()));
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, extents, List.of(view, view)));
		}

		@Test
		void createInvalidAttachmentFormat() {
			final ImageDescriptor invalid = new ImageDescriptor.Builder()
					.extents(new ImageExtents(extents))
					.format(VkFormat.UNDEFINED)
					.aspect(VkImageAspect.COLOR)
					.build();
			when(view.image().descriptor()).thenReturn(invalid);
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, extents, List.of(view)));
		}

		@Test
		void createInvalidAttachmentExtents() {
			final ImageDescriptor invalid = new ImageDescriptor.Builder()
					.extents(new ImageExtents(1, 2))
					.format(FORMAT)
					.aspect(VkImageAspect.COLOR)
					.build();
			when(view.image().descriptor()).thenReturn(invalid);
			assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(pass, extents, List.of(view)));
		}
	}
}
