package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.util.EnumMask;

public class SubResourceTest {
	private Descriptor descriptor;
	private SubResource res;

	@BeforeEach
	void before() {
		descriptor = new Descriptor.Builder()
				.aspect(VkImageAspect.DEPTH)
				.aspect(VkImageAspect.STENCIL)
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.mipLevels(3)
				.arrayLayers(4)
				.extents(new Dimensions(5, 6))
				.build();

		res = new SubResource.Builder(descriptor)
				.aspect(VkImageAspect.DEPTH)
				.levelCount(2)
				.mipLevel(1)
				.layerCount(3)
				.baseArrayLayer(2)
				.build();
	}

	@Test
	void constructor() {
		assertEquals(Set.of(VkImageAspect.DEPTH), res.aspects());
		assertEquals(2, res.levelCount());
		assertEquals(1, res.mipLevel());
		assertEquals(3, res.layerCount());
		assertEquals(2, res.baseArrayLayer());
	}

	@DisplayName("A sub-resource can be converted to a Vulkan layers structure")
	@Test
	void toLayers() {
		final VkImageSubresourceLayers layers = SubResource.toLayers(res);
		assertNotNull(layers);
		assertEquals(new EnumMask<>(VkImageAspect.DEPTH), layers.aspectMask);
		assertEquals(1, layers.mipLevel);
		assertEquals(2, layers.baseArrayLayer);
		assertEquals(3, layers.layerCount);
	}

	@DisplayName("A sub-resource can be converted to a Vulkan ranges structure")
	@Test
	void toRange() {
		final VkImageSubresourceRange range = SubResource.toRange(res);
		assertNotNull(range);
		assertEquals(new EnumMask<>(VkImageAspect.DEPTH), range.aspectMask);
		assertEquals(1, range.baseMipLevel);
		assertEquals(2, range.levelCount);
		assertEquals(2, range.baseArrayLayer);
		assertEquals(3, range.layerCount);
	}

	@DisplayName("A derived sub-resource...")
	@Nested
	class BuilderTests {
		private SubResource.Builder builder;

		@BeforeEach
		void before() {
			builder = new SubResource.Builder(descriptor);
		}

		@DisplayName("can be constructed as a subset of the parent sub-resource or image")
		@Test
		void build() {
			final SubResource subset = builder
					.aspect(VkImageAspect.DEPTH)
					.mipLevel(2)
					.levelCount(1)
					.baseArrayLayer(1)
					.layerCount(1)
					.build();

			assertEquals(Set.of(VkImageAspect.DEPTH), subset.aspects());
			assertEquals(2, subset.mipLevel());
			assertEquals(1, subset.levelCount());
			assertEquals(1, subset.baseArrayLayer());
			assertEquals(1, subset.layerCount());
		}

		@DisplayName("must have a subset of the aspects of the image")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.aspect(VkImageAspect.COLOR));
		}

		@DisplayName("must have a base MIP level that is a subset of the image")
		@Test
		void mip() {
			assertThrows(IllegalArgumentException.class, () -> builder.mipLevel(3));
		}

		@DisplayName("must have a number of MIP levels that are a subset of image")
		@Test
		void levels() {
			assertThrows(IllegalArgumentException.class, () -> builder.levelCount(0));
			assertThrows(IllegalArgumentException.class, () -> builder.levelCount(4));
		}

		@DisplayName("must have a base array layer that is a subset of the image")
		@Test
		void base() {
			assertThrows(IllegalArgumentException.class, () -> builder.baseArrayLayer(4));
		}

		@DisplayName("must have a number of array layers that is a subset of the image")
		@Test
		void layers() {
			assertThrows(IllegalArgumentException.class, () -> builder.layerCount(0));
			assertThrows(IllegalArgumentException.class, () -> builder.layerCount(5));
		}
	}
}
