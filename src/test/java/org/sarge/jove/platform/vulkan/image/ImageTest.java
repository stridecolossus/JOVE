package org.sarge.jove.platform.vulkan.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkImageAspect.COLOR;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;

public class ImageTest extends AbstractVulkanTest {
	private static final Dimensions EXTENTS = new Dimensions(3, 4);

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
	void destroy() {
		image.destroy();
		verify(lib).vkDestroyImage(dev, image, null);
		verify(mem).destroy();
	}

	@Test
	void cubemap() {
		assertEquals(6, Image.CUBEMAP_ARRAY_LAYERS);
	}

	@Nested
	class BuilderTests {
		private Image.Builder builder;
		private AllocationService allocator;
		private MemoryProperties<VkImageUsageFlag> props;

		@BeforeEach
		void before() {
			// Init image memory properties
			props = new MemoryProperties.Builder<VkImageUsageFlag>()
					.mode(VkSharingMode.CONCURRENT)
					.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
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
			info.extent = descriptor.extents().toExtent();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = VkSampleCount.COUNT_4;
			info.tiling = VkImageTiling.LINEAR;
			info.initialLayout = VkImageLayout.PREINITIALIZED;
			info.usage = IntegerEnumeration.reduce(props.usage());
			info.sharingMode = props.mode();
			verify(lib).vkCreateImage(dev, info, null, factory.pointer());

			// TODO
			//verify(lib).vkGetImageMemoryRequirements(DEVICE, POINTER.getValue(), new VkMemoryRequirements());

			// Check bind memory API
			verify(lib).vkBindImageMemory(dev, factory.pointer().getValue(), mem, 0L);
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
