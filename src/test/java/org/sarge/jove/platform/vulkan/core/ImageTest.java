package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
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
	private static final Set<VkImageAspectFlag> COLOUR = Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT);

	private DefaultImage image;
	private Handle handle;
	private Descriptor descriptor;
	private Pointer mem;

	@BeforeEach
	void before() {
		// Create descriptor
		descriptor = new Descriptor.Builder()
				.format(FORMAT)
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
			assertEquals(FORMAT, descriptor.format());
			assertEquals(new Extents(3, 4), descriptor.extents());
			assertEquals(COLOUR, descriptor.aspects());
		}

		@DisplayName("Image must have at least aspect")
		@Test
		void emptyAspects() {
			assertThrows(IllegalArgumentException.class, "must have at least one aspect", () -> new Descriptor(VkImageType.VK_IMAGE_TYPE_2D, FORMAT, new Extents(3, 4), Set.of()));
		}

		@DisplayName("Image aspects must be a valid combination")
		@Test
		void invalidAspects() {
			final var aspects = Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT);
			assertThrows(IllegalArgumentException.class, "Invalid image aspects", () -> new Descriptor(VkImageType.VK_IMAGE_TYPE_2D, FORMAT, new Extents(3, 4), aspects));
		}

		@DisplayName("2D image must have depth of one")
		@Test
		void invalidExtentsDepth() {
			assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new Descriptor(VkImageType.VK_IMAGE_TYPE_2D, FORMAT, new Extents(3, 4, 5), COLOUR));
		}

		@DisplayName("2D image must have height and depth of one")
		@Test
		void invalidExtentsHeightDepth() {
			assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new Descriptor(VkImageType.VK_IMAGE_TYPE_1D, FORMAT, new Extents(3, 4, 5), COLOUR));
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
		void populateRectangle() {
			final VkRect2D rect = new VkRect2D();
			extents.populate(rect);
			assertEquals(0, rect.offset.x);
			assertEquals(0, rect.offset.y);
			assertEquals(2, rect.extent.width);
			assertEquals(3, rect.extent.height);
		}

		@Test
		void populateExtents() {
			final VkExtent3D result = new VkExtent3D();
			extents.populate(result);
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
		private MemoryAllocator allocator;

		@BeforeEach
		void before() {
			// Init memory allocator
			allocator = mock(MemoryAllocator.class);
			when(dev.allocator()).thenReturn(allocator);

			// Create builder
			builder = new Image.Builder(dev);
		}

		@Test
		void build() {
			// Init image memory
			final Pointer mem = new Pointer(1);
			final MemoryAllocator.Allocation allocation = mock(MemoryAllocator.Allocation.class);
			when(allocator.allocation()).thenReturn(allocation);
			when(allocation.allocate()).thenReturn(mem);
			when(allocation.init(any())).thenReturn(allocation);

			// Init memory requirements for the image
			final Answer<Void> answer = inv -> {
				final VkMemoryRequirements reqs = inv.getArgument(2);
				reqs.size = 2;
				return null;
			};
			doAnswer(answer).when(lib).vkGetImageMemoryRequirements(any(), any(), isA(VkMemoryRequirements.class)); // TODO - FFS this is annoying

			// Build image
			final Image image = new Image.Builder(dev)
				.type(VkImageType.VK_IMAGE_TYPE_2D)
				.format(FORMAT)
				.extents(new Image.Extents(1, 2))
				.mipLevels(3)
				.arrayLayers(4)
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
			assertEquals(VkImageType.VK_IMAGE_TYPE_2D, descriptor.type());
			assertEquals(FORMAT, descriptor.format());
			assertEquals(new Extents(1, 2), descriptor.extents());
			assertEquals(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, VkImageAspectFlag.VK_IMAGE_ASPECT_STENCIL_BIT), descriptor.aspects());

			// Check API
			final ArgumentCaptor<VkImageCreateInfo> captor = ArgumentCaptor.forClass(VkImageCreateInfo.class);
			verify(lib).vkCreateImage(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

			// Check create image descriptor
			final VkImageCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(VkImageType.VK_IMAGE_TYPE_2D, info.imageType);
			assertEquals(FORMAT, info.format);
			assertNotNull(info.extent);
			assertEquals(1, info.extent.width);
			assertEquals(2, info.extent.height);
			assertEquals(1, info.extent.depth);
			assertEquals(3, info.mipLevels);
			assertEquals(4, info.arrayLayers);
			assertEquals(VkImageTiling.VK_IMAGE_TILING_OPTIMAL, info.tiling);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, info.initialLayout);
			assertEquals(IntegerEnumeration.mask(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_SRC_BIT, VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT), info.usage);
			assertEquals(VkSampleCountFlag.VK_SAMPLE_COUNT_2_BIT, info.samples);
			assertEquals(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE, info.sharingMode);

			// Check memory allocation
			verify(lib).vkBindImageMemory(dev.handle(), image.handle(), mem, 0);
		}

		@Test
		void buildRequiresFormat() {
			assertThrows(IllegalArgumentException.class, "No image format", () -> builder.build());
		}

		@Test
		void buildRequiresExtents() {
			builder.format(FORMAT);
			assertThrows(IllegalArgumentException.class, "No image extents", () -> builder.build());
		}

		@Test
		void buildInvalidArrayLayers() {
			final var builder = new Image.Builder(dev)
					.type(VkImageType.VK_IMAGE_TYPE_3D)
					.format(FORMAT)
					.extents(new Image.Extents(1, 2, 3))
					.arrayLayers(2)
					.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT);

			assertThrows(IllegalArgumentException.class, "must be one for a 3D image", () -> builder.build());
		}
	}
}
