package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.texture.Sampler;

import com.sun.jna.Pointer;

public class ImageViewTest extends AbstractVulkanTest {
	private ImageView view;
	private VulkanImage image;

	@BeforeEach
	public void before() {
		image = mock(VulkanImage.class);
		view = new ImageView(mock(Pointer.class), device, image);
	}

	@Test
	public void constructor() {
		assertEquals(image, view.image());
	}

	@Test
	public void sampler() {
		final Sampler.Descriptor descriptor = new Sampler.Descriptor.Builder().build();
		final Sampler sampler = view.sampler(descriptor);
		assertNotNull(sampler);
		// TODO
		//verify(library).vkCreateSampler(device, pCreateInfo, pAllocator, pSampler)
	}

	@Test
	public void destroy() {
		final Pointer handle = view.handle();
		view.destroy();
		verify(library).vkDestroyImageView(device.handle(), handle, null);
	}

	@Nested
	class BuilderTests {

	}
}
