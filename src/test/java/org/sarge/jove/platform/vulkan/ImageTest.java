package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.Image.Extents;

import com.sun.jna.Pointer;

public class ImageTest {
	private Image image;
	private LogicalDevice dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create device
		dev = mock(LogicalDevice.class);
		when(dev.library()).thenReturn(lib);

		// Create image
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
		image.destroy();
		verify(lib).vkDestroyImage(dev.handle(), image.handle(), null);
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

