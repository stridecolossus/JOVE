package org.sarge.jove.platform.vulkan.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.VkImageAspect.COLOR;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.image.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ImageTest extends AbstractVulkanTest {
	private static final Set<VkImageAspect> COLOUR = Set.of(COLOR);
	private static final ImageExtents EXTENTS = new ImageExtents(3, 4);

	private DefaultImage image;
	private Pointer handle;
	private ImageDescriptor descriptor;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		// Create descriptor
		descriptor = new ImageDescriptor.Builder()
				.format(FORMAT)
				.extents(EXTENTS)
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

	@Test
	void close() {
		image.destroy();
		verify(lib).vkDestroyImage(dev, image, null);
		verify(mem).destroy();
	}

	@Nested
	class DepthFormatTests {
		private PhysicalDevice dev;

		@BeforeEach
		void before() {
			dev = mock(PhysicalDevice.class);
			when(dev.properties(any(VkFormat.class))).thenReturn(new VkFormatProperties());
		}

		@Test
		void depth() {
			final VkFormatProperties props = new VkFormatProperties();
			props.optimalTilingFeatures = VkFormatFeature.DEPTH_STENCIL_ATTACHMENT.value();
			when(dev.properties(VkFormat.D32_SFLOAT_S8_UINT)).thenReturn(props);
			assertEquals(VkFormat.D32_SFLOAT_S8_UINT, Image.depth(dev));
		}

		@Test
		void depthUnsupported() {
			assertThrows(RuntimeException.class, () -> Image.depth(dev));
		}
	}

	@Nested
	class BuilderTests {
		private Image.Builder builder;
		private AllocationService allocator;
		private MemoryProperties<VkImageUsage> props;

		@BeforeEach
		void before() {
			// Init image memory properties
			props = new MemoryProperties.Builder()
					.mode(VkSharingMode.CONCURRENT)
					.usage(VkImageUsage.COLOR_ATTACHMENT)
					.build();

			// Init memory allocator
			allocator = mock(AllocationService.class);
			when(allocator.allocate(isA(VkMemoryRequirements.class), eq(props))).thenReturn(mem);

			// Create builder
			builder = new Image.Builder();
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
					.cubemap()
					.build(dev, allocator);

			// Check image
			assertNotNull(image);
			assertEquals(new Handle(POINTER.getValue()), image.handle());
			assertEquals(descriptor, image.descriptor());
			assertEquals(dev, image.device());
			assertEquals(false, image.isDestroyed());

			// Check create image API
			final VkImageCreateInfo info = new VkImageCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((VkImageCreateInfo) obj, true);
				}
			};
			info.flags = VkImageCreateFlag.CUBE_COMPATIBLE.value();
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent3D();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = VkSampleCountFlag.COUNT_4;
			info.tiling = VkImageTiling.LINEAR;
			info.initialLayout = VkImageLayout.PREINITIALIZED;
			info.usage = IntegerEnumeration.mask(props.usage());
			info.sharingMode = props.mode();
			verify(lib).vkCreateImage(dev, info, null, POINTER);

			// TODO
			//verify(lib).vkGetImageMemoryRequirements(DEVICE, POINTER.getValue(), new VkMemoryRequirements());

			// Check bind memory API
			verify(lib).vkBindImageMemory(dev, POINTER.getValue(), mem, 0L);
		}

		@Test
		void buildEmptyMemoryProperties() {
			builder.descriptor(descriptor);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev, allocator));
		}

		@Test
		void buildEmptyImageDescriptor() {
			builder.properties(props);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev, allocator));
		}

		@Test
		void buildInvalidLayout() {
			assertThrows(IllegalArgumentException.class, () -> builder.initialLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
		}
	}
}
