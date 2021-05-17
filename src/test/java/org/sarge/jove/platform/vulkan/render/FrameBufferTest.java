package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class FrameBufferTest extends AbstractVulkanTest {
	private FrameBuffer buffer;
	private View view;
	private RenderPass pass;

	@BeforeEach
	void before() {
		// Create render pass
		pass = mock(RenderPass.class);
		when(pass.handle()).thenReturn(new Handle(new Pointer(1)));
		when(pass.device()).thenReturn(dev);
		when(pass.count()).thenReturn(1);

		// Init image descriptor
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.extents(new Image.Extents(3, 4))
				.format(FORMAT)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		final Image image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create swapchain attachment
		view = mock(View.class);
		when(view.handle()).thenReturn(new Handle(new Pointer(2)));
		when(view.image()).thenReturn(image);

		// Create buffer
		buffer = FrameBuffer.create(List.of(view), pass);
	}

	@Test
	void constructor() {
		assertNotNull(buffer);
		assertNotNull(buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(view.image().descriptor().extents(), buffer.extents());
		assertEquals(List.of(view), buffer.attachments());
	}

	@Test
	void create() {
		// Check allocation
		final ArgumentCaptor<VkFramebufferCreateInfo> captor = ArgumentCaptor.forClass(VkFramebufferCreateInfo.class);
		verify(lib).vkCreateFramebuffer(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check descriptor
		final var info = captor.getValue();
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
		assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(List.of(), pass));
		assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(List.of(view, view), pass));
	}

	@Test
	void createInvalidExtents() {
		// Create image with different extents
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.extents(new Image.Extents(5, 6))
				.format(FORMAT)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create second attachment
		final View other = mock(View.class);
		when(other.image()).thenReturn(mock(Image.class));
		when(other.image().descriptor()).thenReturn(descriptor);

		// Check different extents
		when(pass.count()).thenReturn(2);
		assertThrows(IllegalArgumentException.class, () -> FrameBuffer.create(List.of(view, other), pass));
	}

	@Test
	void destroy() {
		final Handle handle = buffer.handle();
		buffer.destroy();
		verify(lib).vkDestroyFramebuffer(dev.handle(), handle, null);
	}
}
