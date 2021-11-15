package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageSubresourceLayers;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerEnumeration;

public class SubResourceTest {
	private ImageDescriptor descriptor;
	private SubResource res;

	@BeforeEach
	void before() {
		descriptor = new ImageDescriptor.Builder()
				.aspect(VkImageAspect.DEPTH)
				.aspect(VkImageAspect.STENCIL)
				.format(AbstractVulkanTest.FORMAT)
				.mipLevels(3)
				.arrayLayers(4)
				.extents(new ImageExtents(5, 6))
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
		assertNotNull(res);
	}

	@Test
	void toLayers() {
		final VkImageSubresourceLayers layers = SubResource.toLayers(res);
		assertNotNull(layers);
		assertEquals(VkImageAspect.DEPTH.value(), layers.aspectMask);
		assertEquals(1, layers.mipLevel);
		assertEquals(2, layers.baseArrayLayer);
		assertEquals(3, layers.layerCount);
	}

	@Test
	void toRange() {
		final VkImageSubresourceRange range = SubResource.toRange(res);
		assertNotNull(range);
		assertEquals(VkImageAspect.DEPTH.value(), range.aspectMask);
		assertEquals(1, range.baseMipLevel);
		assertEquals(2, range.levelCount);
		assertEquals(2, range.baseArrayLayer);
		assertEquals(3, range.layerCount);
	}

	@Nested
	class BuilderTests {
		private SubResource.Builder builder;

		@BeforeEach
		void before() {
			builder = new SubResource.Builder(descriptor);
		}

		@Test
		void build() {
			final VkImageSubresourceRange range = SubResource.toRange(builder.build());
			assertNotNull(range);
			assertEquals(IntegerEnumeration.mask(VkImageAspect.DEPTH, VkImageAspect.STENCIL), range.aspectMask);
			assertEquals(0, range.baseMipLevel);
			assertEquals(1, range.levelCount);
			assertEquals(0, range.baseArrayLayer);
			assertEquals(1, range.layerCount);
		}

		@DisplayName("Specifying an explicit aspect should override the image mask")
		@Test
		void buildOverrideAspect() {
			builder.aspect(VkImageAspect.DEPTH);
			final VkImageSubresourceRange range = SubResource.toRange(builder.build());
			assertNotNull(range);
			assertEquals(VkImageAspect.DEPTH.value(), range.aspectMask);
		}

		@DisplayName("Aspect mask must be a subset of the image")
		@Test
		void buildInvalidAspect() {
			assertThrows(IllegalArgumentException.class, () -> builder.aspect(VkImageAspect.COLOR));
		}

		@DisplayName("Base MIP level must be a subset of the image")
		@Test
		void buildInvalidLevel() {
			assertThrows(IllegalArgumentException.class, () -> builder.mipLevel(3));
		}

		@DisplayName("Number of MIP levels must be a subset of the image")
		@Test
		void buildInvalidLevelCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.levelCount(0));
			assertThrows(IllegalArgumentException.class, () -> builder.levelCount(4));
		}

		@DisplayName("Base array layer must be a subset of the image")
		@Test
		void buildInvalidLayer() {
			assertThrows(IllegalArgumentException.class, () -> builder.baseArrayLayer(4));
		}

		@DisplayName("Number of array layers must be a subset of the image")
		@Test
		void buildInvalidLayerCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.layerCount(0));
			assertThrows(IllegalArgumentException.class, () -> builder.layerCount(5));
		}
	}
}
