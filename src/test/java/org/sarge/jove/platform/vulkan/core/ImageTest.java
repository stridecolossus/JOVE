package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.core.Image.Extents;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ImageTest extends AbstractVulkanTest {
	private Image image;

	@BeforeEach
	void before() {
		image = new Image(new Pointer(42), dev, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, new Extents(1, 2), Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));
	}

	@Test
	void constructor() {
		assertNotNull(image.handle());
		assertEquals(dev, image.device());
		assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, image.format());
		assertEquals(new Extents(1, 2), image.extents());
		assertEquals(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT), image.aspect());
		assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, image.layout());
	}

	@Test
	void view() {
		final View view = image.view();
		assertNotNull(view);
		assertEquals(image, view.image());
	}

	@Test
	void destroy() {
		final Handle handle = image.handle();
		image.destroy();
		verify(lib).vkDestroyImage(dev.handle(), handle, null);
	}

	@Nested
	class ExtentsTest {
		private Extents extents;

		@BeforeEach
		void before() {
			extents = new Extents(2, 3);
		}

		@Test
		void constructor() {
			assertEquals(1, extents.depth());
			assertEquals(2, extents.width());
			assertEquals(3, extents.height());
		}

		@Test
		void create() {
			final var result = extents.create();
			assertNotNull(result);
			assertEquals(1, result.depth);
			assertEquals(2, result.width);
			assertEquals(3, result.height);
		}

		@Test
		void dimensions() {
			final var result = Extents.of(new Dimensions(2, 3));
			assertNotNull(result);
			assertTrue(result.equals(extents));
		}
	}
}

//	@Test
//	public void builder() {
//		final Image image = new Image.Builder(device)
//			.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
//			.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_STENCIL_BIT)
//			.type(VkImageType.VK_IMAGE_TYPE_3D)
//			.format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT)
//			.extents(Image.extents(1, 2, 3))
//			.mipLevels(4)
//			.arrayLayers(5)
//			.tiling(VkImageTiling.VK_IMAGE_TILING_LINEAR)
//			.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED)
//			.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
//			.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
//			.samples(VkSampleCountFlag.VK_SAMPLE_COUNT_2_BIT)
//			.mode(VkSharingMode.VK_SHARING_MODE_CONCURRENT)
//			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT)
//			.build();
//
//		assertNotNull(image);
//		assertEquals(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, VkImageAspectFlag.VK_IMAGE_ASPECT_STENCIL_BIT), image.aspect());
//		assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, image.format());
//		assertTrue(Image.extents(1, 2, 3).dataEquals(image.extents()));
//		assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, image.layout());
//	}

