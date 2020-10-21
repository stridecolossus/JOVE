package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.core.Image.Descriptor;
import org.sarge.jove.platform.vulkan.core.Image.Extents;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ImageTest extends AbstractVulkanTest {
	private DefaultImage image;
	private Handle handle;
	private Descriptor descriptor;
	private Pointer mem;

	@BeforeEach
	void before() {
		// Create descriptor
		descriptor = new Image.Descriptor.Builder()
				.format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT)
				.extents(new Image.Extents(3, 4))
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.build();

		// Create image
		handle = new Handle(new Pointer(1));
		mem = new Pointer(2);
		image = new DefaultImage(handle, descriptor, mem, dev);
	}

	@Test
	void constructor() {
		assertEquals(handle, image.handle());
		assertEquals(dev, image.device());
		assertEquals(descriptor, image.descriptor());
	}

	@Nested
	class DescriptorTests {
		@Test
		void constructor() {
			assertEquals(VkImageType.VK_IMAGE_TYPE_2D, descriptor.type());
			assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, descriptor.format());
			assertEquals(new Extents(3, 4), descriptor.extents());
			assertEquals(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT), descriptor.aspects());
		}

		@Test
		void constructorRequiresFormat() {
			final var builder = new Image.Descriptor.Builder().extents(new Extents(3, 4));
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void constructorRequiresExtents() {
			final var builder = new Image.Descriptor.Builder().format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}

	@Test
	void destroy() {
		image.destroy();
		verify(lib).vkDestroyImage(dev.handle(), handle, null);
		verify(lib).vkFreeMemory(dev.handle(), mem, null);
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

	@Nested
	class BuilderTests {
		private Image.Builder builder;

		@BeforeEach
		void before() {
			builder = new Image.Builder(dev);
		}

		@Test
		void build() {
			// Init image memory
			final Pointer mem = new Pointer(42);
			when(dev.allocate(isA(VkMemoryRequirements.class), eq(Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT)))).thenReturn(mem);

			// Build image
			final Image image = new Image.Builder(dev)
				.type(VkImageType.VK_IMAGE_TYPE_3D)
				.format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT)
				.extents(new Image.Extents(1, 2, 3))
				.mipLevels(4)
				.arrayLayers(5)
				.tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL)
				.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.samples(VkSampleCountFlag.VK_SAMPLE_COUNT_2_BIT)
				.mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_STENCIL_BIT)
				.build();

			// Check image
			assertNotNull(image);
			assertNotNull(image.handle());

			// Check descriptor
			descriptor = image.descriptor();
			assertNotNull(descriptor);
			assertEquals(VkImageType.VK_IMAGE_TYPE_3D, descriptor.type());
			assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, descriptor.format());
			assertEquals(new Extents(1, 2, 3), descriptor.extents());
			assertEquals(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, VkImageAspectFlag.VK_IMAGE_ASPECT_STENCIL_BIT), descriptor.aspects());

			// Check API
			final ArgumentCaptor<VkImageCreateInfo> captor = ArgumentCaptor.forClass(VkImageCreateInfo.class);
			verify(lib).vkCreateImage(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

			// Check create image descriptor
			final VkImageCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(VkImageType.VK_IMAGE_TYPE_3D, info.imageType);
			assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, info.format);
			assertNotNull(info.extent);
			assertEquals(1, info.extent.width);
			assertEquals(2, info.extent.height);
			assertEquals(3, info.extent.depth);
			assertEquals(4, info.mipLevels);
			assertEquals(5, info.arrayLayers);
			assertEquals(VkImageTiling.VK_IMAGE_TILING_OPTIMAL, info.tiling);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, info.initialLayout);
			assertEquals(IntegerEnumeration.mask(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_SRC_BIT, VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT), info.usage);
			assertEquals(VkSampleCountFlag.VK_SAMPLE_COUNT_2_BIT, info.samples);
			assertEquals(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE, info.sharingMode);

			// Check memory allocation
			verify(lib).vkGetImageMemoryRequirements(eq(dev.handle()), eq(image.handle()), isA(VkMemoryRequirements.class));
			verify(lib).vkBindImageMemory(dev.handle(), image.handle(), mem, 0);
		}

		@Test
		void buildRequiresFormat() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildRequiresExtents() {
			builder.format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
