package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

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

		// Create swapchain image-view
		view = mock(View.class);
		when(view.handle()).thenReturn(new Handle(new Pointer(2)));

		// Init image descriptor
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.extents(new Image.Extents(3, 4))
				.format(FORMAT)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.build();

		// Create image
		final Image image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create swapchain image
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
		verify(lib).vkCreateFramebuffer(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

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
	void destroy() {
		final Handle handle = buffer.handle();
		buffer.destroy();
		verify(lib).vkDestroyFramebuffer(dev.handle(), handle, null);
	}
}
