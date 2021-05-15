package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.VkImageAspect.COLOR;
import static org.sarge.jove.platform.vulkan.VkImageType.IMAGE_TYPE_2D;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.core.Image.Descriptor;
import org.sarge.jove.platform.vulkan.core.Image.Descriptor.SubResourceBuilder;
import org.sarge.jove.platform.vulkan.core.Image.Extents;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class ImageTest extends AbstractVulkanTest {
	private static final Set<VkImageAspect> COLOUR = Set.of(COLOR);

	private DefaultImage image;
	private Pointer handle;
	private Descriptor descriptor;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		// Create descriptor
		descriptor = new Descriptor.Builder()
				.format(FORMAT)
				.extents(new Image.Extents(3, 4))
				.aspect(COLOR)
				.mipLevels(2)
				.arrayLayers(3)
				.build();

		// Init image memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));

		// Create image
		handle = new Pointer(2);
		image = new DefaultImage(handle, dev, descriptor, mem);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(handle), image.handle());
		assertEquals(dev, image.device());
		assertEquals(descriptor, image.descriptor());
	}

	@Nested
	class DescriptorTests {
		@Test
		void constructor() {
			assertEquals(IMAGE_TYPE_2D, descriptor.type());
			assertEquals(FORMAT, descriptor.format());
			assertEquals(new Extents(3, 4), descriptor.extents());
			assertEquals(COLOUR, descriptor.aspects());
		}

		@DisplayName("Image must have at least one aspect")
		@Test
		void emptyAspects() {
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(IMAGE_TYPE_2D, FORMAT, new Extents(3, 4), Set.of(), 1, 1));
		}

		@DisplayName("Image aspects must be a valid combination")
		@Test
		void invalidAspects() {
			final var aspects = Set.of(COLOR, VkImageAspect.DEPTH);
			assertThrows(IllegalArgumentException.class, "Invalid image aspects", () -> new Descriptor(IMAGE_TYPE_2D, FORMAT, new Extents(3, 4), aspects, 1, 1));
		}

		@DisplayName("2D image must have depth of one")
		@Test
		void invalidExtentsDepth() {
			assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new Descriptor(IMAGE_TYPE_2D, FORMAT, new Extents(3, 4, 5), COLOUR, 1, 1));
		}

		@DisplayName("2D image must have height and depth of one")
		@Test
		void invalidExtentsHeightDepth() {
			assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new Descriptor(VkImageType.IMAGE_TYPE_1D, FORMAT, new Extents(3, 4, 5), COLOUR, 1, 1));
		}

		@DisplayName("3D image can only have one array layer")
		@Test
		void invalidArrayLayers() {
			assertThrows(IllegalArgumentException.class, "Array layers must be one", () -> new Descriptor(VkImageType.IMAGE_TYPE_3D, FORMAT, new Extents(3, 4, 5), COLOUR, 1, 2));
		}
	}

	@Test
	void destroy() {
		image.destroy();
		verify(lib).vkDestroyImage(dev.handle(), image.handle(), null);
		verify(mem).destroy();
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
		private MemoryProperties<VkImageUsage> props;

		@BeforeEach
		void before() {
			when(dev.allocate(any(VkMemoryRequirements.class), any(MemoryProperties.class))).thenReturn(mem);
			builder = new Image.Builder();
			props = new MemoryProperties.Builder()
					.mode(VkSharingMode.CONCURRENT)
					.usage(VkImageUsage.COLOR_ATTACHMENT)
					.build();
		}

		@Test
		void build() {
			// Create image
			image = builder
					.descriptor(descriptor)
					.properties(props)
					.samples(4)
					.tiling(VkImageTiling.LINEAR)
					.initialLayout(VkImageLayout.PREINITIALIZED)
					.build(dev);

			// Check image
			assertNotNull(image);
			assertNotNull(image.handle());
			assertEquals(descriptor, image.descriptor());
			assertEquals(dev, image.device());
			assertEquals(false, image.isDestroyed());

			// Check API
			final ArgumentCaptor<VkImageCreateInfo> captor = ArgumentCaptor.forClass(VkImageCreateInfo.class);
			final PointerByReference ref = lib.factory().pointer();
			verify(lib).vkCreateImage(eq(dev.handle()), captor.capture(), isNull(), eq(ref));
			verify(lib).vkGetImageMemoryRequirements(eq(dev.handle()), eq(ref.getValue()), any(VkMemoryRequirements.class));
			verify(lib).vkBindImageMemory(eq(dev.handle()), eq(ref.getValue()), any(Handle.class), eq(0L));

			// Check create descriptor
			final VkImageCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(IMAGE_TYPE_2D, info.imageType);
			assertEquals(FORMAT, info.format);
			assertEquals(2, info.mipLevels);
			assertEquals(3, info.arrayLayers);
			assertEquals(VkSampleCountFlag.VK_SAMPLE_COUNT_4, info.samples);
			assertEquals(VkImageTiling.LINEAR, info.tiling);
			assertEquals(VkImageLayout.PREINITIALIZED, info.initialLayout);
			assertEquals(VkImageUsage.COLOR_ATTACHMENT.value(), info.usage);
			assertEquals(VkSharingMode.CONCURRENT, info.sharingMode);

			// Check extents
			assertNotNull(info.extent);
			assertEquals(3, info.extent.width);
			assertEquals(4, info.extent.height);
			assertEquals(1, info.extent.depth);
		}

		@Test
		void buildEmptyMemoryProperties() {
			builder.descriptor(descriptor);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@Test
		void buildEmptyImageDescriptor() {
			builder.properties(props);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}

	@Nested
	class SubResourceBuilderTests {
		private SubResourceBuilder<Object> builder;

		@BeforeEach
		void before() {
			builder = descriptor.builder(new Object());
		}

		@Test
		void constructor() {
			assertNotNull(builder);
		}

		@Test
		void mipLevelInvalid() {
			assertThrows(IllegalArgumentException.class, "mip level", () -> builder.mipLevel(3));
		}

		@Test
		void baseArrayLayerInvalid() {
			assertThrows(IllegalArgumentException.class, "base array layer", () -> builder.baseArrayLayer(4));
		}

		@Test
		void build() {
			assertNotNull(builder.build());
		}

		@Test
		void populate() {
			// Configure sub-resource
			builder.mipLevel(1);
			builder.baseArrayLayer(2);

			// Populate range
			final var range = new VkImageSubresourceRange();
			builder.populate(range);
			assertEquals(1, range.baseMipLevel);
			assertEquals(2, range.baseArrayLayer);
// TODO - remaining levels/layers
//			assertEquals(SubResourceBuilder.REMAINING, range.levelCount);
//			assertEquals(SubResourceBuilder.REMAINING, range.layerCount);
			assertEquals(1, range.levelCount);
			assertEquals(1, range.layerCount);
			assertEquals(VkImageAspect.COLOR.value(), range.aspectMask);

			// Populate layers
			final var layers = new VkImageSubresourceLayers();
			builder.populate(layers);
			assertEquals(1, layers.mipLevel);
			assertEquals(2, layers.baseArrayLayer);
//			assertEquals(SubResourceBuilder.REMAINING, range.layerCount);
			assertEquals(1, range.layerCount);
			assertEquals(VkImageAspect.COLOR.value(), layers.aspectMask);
		}
	}
}
