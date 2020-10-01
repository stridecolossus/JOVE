package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
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

		// Create swapchain image
		final Image image = mock(Image.class);
		when(image.extents()).thenReturn(new Image.Extents(3, 4));

		// Create swapchain image-view
		view = mock(View.class);
		when(view.handle()).thenReturn(new Handle(new Pointer(2)));
		when(view.image()).thenReturn(image);
		when(view.device()).thenReturn(dev);

		// Create buffer
		buffer = FrameBuffer.create(view, pass);
	}

	@Test
	void constructor() {
		assertNotNull(buffer);
		assertNotNull(buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(view, buffer.view());
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
