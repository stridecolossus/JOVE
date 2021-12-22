package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.DepthClearValue;
import org.sarge.jove.platform.vulkan.image.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ViewTest extends AbstractVulkanTest {
	private View view;
	private DefaultImage image;
	private ClearValue clear;

	@BeforeEach
	void before() {
		// Create image descriptor
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.format(FORMAT)
				.extents(new Extents(new Dimensions(3, 4)))
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		image = mock(DefaultImage.class);
		when(image.descriptor()).thenReturn(descriptor);
		when(image.handle()).thenReturn(new Handle(1));

		// Create image view
		view = new View(new Pointer(2), image, dev);

		// Init clear value
		clear = new ColourClearValue(Colour.WHITE);
	}

	@Test
	void constructor() {
		assertNotNull(view.handle());
		assertEquals(false, view.isDestroyed());
		assertEquals(dev, view.device());
		assertEquals(image, view.image());
		assertEquals(ClearValue.NONE, view.clear());
	}

	@Test
	void clear() {
		view.clear(clear);
		assertEquals(clear, view.clear());
	}

	@Test
	void clearNone() {
		view.clear(ClearValue.NONE);
	}

	@Test
	void clearInvalidAspect() {
		assertThrows(IllegalArgumentException.class, () -> view.clear(DepthClearValue.DEFAULT));
	}

	@Test
	void destroy() {
		view.destroy();
		verify(lib).vkDestroyImageView(dev, view, null);
		verify(image).destroy();
		verifyNoMoreInteractions(lib);
	}

	@Test
	void of() {
		when(image.device()).thenReturn(dev);
		assertNotNull(View.of(image));
	}

	@Nested
	class BuilderTests {
		private View.Builder builder;

		@BeforeEach
		void before() {
			when(image.device()).thenReturn(dev);
			builder = new View.Builder(image);
		}

		@Test
		void build() {
			// Init image sub-resource
			final SubResource res = new SubResource.Builder(image.descriptor()).build();

			// Build view
			view = builder
					.type(VkImageViewType.CUBE)
					.mapping(ComponentMapping.of("BGRA"))
					.subresource(res)
					.build();

			// Check view
			assertNotNull(view);
			assertNotNull(view.handle());
			assertEquals(image, view.image());

			// Init expected descriptor
			final var expected = new VkImageViewCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check descriptor
					final var info = (VkImageViewCreateInfo) obj;
					assertNotNull(info);
					assertEquals(0, info.flags);
					assertEquals(ViewTest.this.image.handle(), info.image);
					assertEquals(VkImageViewType.CUBE, info.viewType);
					assertEquals(FORMAT, info.format);
					assertNotNull(info.subresourceRange);

					// Check component mapping
					assertNotNull(info.components);
					assertEquals(VkComponentSwizzle.B, info.components.r);
					assertEquals(VkComponentSwizzle.G, info.components.g);
					assertEquals(VkComponentSwizzle.R, info.components.b);
					assertEquals(VkComponentSwizzle.A, info.components.a);

					return true;
				}
			};

			// Check API
			verify(lib).vkCreateImageView(dev, expected, null, POINTER);
		}
	}
}
