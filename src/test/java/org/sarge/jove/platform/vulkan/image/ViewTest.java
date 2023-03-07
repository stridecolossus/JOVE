package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.memory.MockDeviceMemory;

public class ViewTest {
	private View view;
	private DefaultImage image;
	private ClearValue clear;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();

		// Create image descriptor
		final Descriptor descriptor = new Descriptor.Builder()
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.extents(new Dimensions(3, 4))
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		image = new DefaultImage(new Handle(2), dev, descriptor, new MockDeviceMemory());

		// Create image view
		view = new View(new Handle(3), dev, image);

		// Init clear value
		clear = new ColourClearValue(Colour.WHITE);
	}

	@Test
	void constructor() {
		assertEquals(false, view.isDestroyed());
		assertEquals(dev, view.device());
		assertEquals(image, view.image());
	}

	@DisplayName("A default view can be constructed for a given image")
	@Test
	void of() {
		view = View.of(image);
		assertEquals(image, view.image());
	}

	@DisplayName("The clear value for a view...")
	@Nested
	class ClearTests {
		@DisplayName("is empty be default")
		@Test
		void unspecified() {
			assertEquals(Optional.empty(), view.clear());
		}

		@DisplayName("can be set to a new clear value")
		@Test
		void clear() {
			view.clear(clear);
			assertEquals(Optional.of(clear), view.clear());
		}

		@DisplayName("can be set to an empty clear value")
		@Test
		void none() {
			view.clear(null);
			assertEquals(Optional.empty(), view.clear());
		}

		@DisplayName("cannot be set to a clear value for a different type of attachment")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> view.clear(DepthClearValue.DEFAULT));
		}
	}

	@DisplayName("An image view can be destroyed")
	@Test
	void destroy() {
		view.destroy();
		verify(dev.library()).vkDestroyImageView(dev, view, null);
		assertEquals(true, image.isDestroyed());
	}

	@DisplayName("The underlying image of the view is destroyed by default")
	@Test
	void auto() {
		view.setDestroyImage(false);
		view.destroy();
		assertEquals(false, image.isDestroyed());
	}

	@Nested
	class BuilderTests {
		private View.Builder builder;

		@BeforeEach
		void before() {
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
			assertNotNull(view.handle());
			assertEquals(image, view.image());

			// Init expected descriptor
			final var expected = new VkImageViewCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check descriptor
					final var info = (VkImageViewCreateInfo) obj;
					assertEquals(null, info.flags);
					assertEquals(ViewTest.this.image.handle(), info.image);
					assertEquals(VkImageViewType.CUBE, info.viewType);
					assertEquals(VkFormat.R32G32B32A32_SFLOAT, info.format);
					assertNotNull(info.subresourceRange);

					// Check component mapping
					assertEquals(VkComponentSwizzle.B, info.components.r);
					assertEquals(VkComponentSwizzle.G, info.components.g);
					assertEquals(VkComponentSwizzle.R, info.components.b);
					assertEquals(VkComponentSwizzle.A, info.components.a);

					return true;
				}
			};

			// Check API
			verify(dev.library()).vkCreateImageView(dev, expected, null, dev.factory().pointer());
		}
	}
}
