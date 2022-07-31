package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class FrameSetTest extends AbstractVulkanTest {
	private static final Dimensions EXTENTS = new Dimensions(2, 3);

	private FrameSet set;
	private Swapchain swapchain;
	private RenderPass pass;
	private View one, two;

	@BeforeEach
	void before() {
		// Define image
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.aspect(VkImageAspect.COLOR)
				.format(FORMAT)
				.extents(new ImageData.Extents(EXTENTS))
				.build();

		// Create image
		final Image image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);
		when(image.handle()).thenReturn(new Handle(3));

		// Create some attachments
		one = mock(View.class);
		two = mock(View.class);
		when(one.handle()).thenReturn(new Handle(1));
		when(two.handle()).thenReturn(new Handle(2));
		when(one.image()).thenReturn(image);
		when(two.image()).thenReturn(image);

		// Define expected attachment
		final Attachment attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		// Create render pass
		pass = mock(RenderPass.class);
		when(pass.attachments()).thenReturn(List.of(attachment, attachment));
		when(pass.device()).thenReturn(dev);

		// Create swapchain
		swapchain = new Swapchain(new Pointer(4), dev, FORMAT, EXTENTS, List.of(one));

		// Create frame-set
		set = new FrameSet(swapchain, pass, List.of(two));
	}

	@Test
	void constructor() {
		assertEquals(swapchain, set.swapchain());
		assertEquals(false, set.isDestroyed());
	}

	@Test
	void buffer() {
		final FrameBuffer buffer = set.buffer(0);
		assertNotNull(buffer);
	}

	@Test
	void destroy() {
		set.destroy();
		assertEquals(true, set.buffer(0).isDestroyed());
	}
}
