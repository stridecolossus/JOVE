package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.sarge.jove.platform.vulkan.VkImageAspect.COLOR;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;

class DefaultImageTest {
	private DefaultImage image;
	private Descriptor descriptor;
	private DeviceMemory mem;
	private VulkanLibrary lib;
	private DeviceContext dev;
	private Allocator allocator;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();
		lib = dev.library();

		// Create descriptor
		descriptor = new Descriptor.Builder()
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.extents(new Dimensions(3, 4))
				.aspect(COLOR)
				.mipLevels(2)
				.arrayLayers(3)
				.build();

		// Init image memory
		mem = new MockDeviceMemory();

		allocator = new MockAllocator();

		// Create image
		image = new DefaultImage(new Handle(2), dev, descriptor, mem);
	}

	@Test
	void constructor() {
		assertEquals(dev, image.device());
		assertEquals(false, image.isDestroyed());
		assertEquals(descriptor, image.descriptor());
		assertEquals(mem, image.memory());
	}

	@Test
	void destroy() {
		image.destroy();
		assertEquals(true, image.isDestroyed());
		assertEquals(true, mem.isDestroyed());
		verify(lib).vkDestroyImage(dev, image, null);
	}

	@Nested
	class BuilderTests {
		private DefaultImage.Builder builder;
		private MemoryProperties<VkImageUsageFlag> props;

		@BeforeEach
		void before() {
			// Init image memory properties
			props = new MemoryProperties.Builder<VkImageUsageFlag>()
					.mode(VkSharingMode.CONCURRENT)
					.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
					.required(VkMemoryProperty.HOST_VISIBLE)
					.build();

			// Create builder
			builder = new DefaultImage.Builder();
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
			assertEquals(descriptor, image.descriptor());
			assertEquals(dev, image.device());
			assertEquals(false, image.isDestroyed());

			// Init expected creation descriptor
			final var info = new VkImageCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((VkImageCreateInfo) obj, true);
				}
			};
			info.flags = EnumMask.of(VkImageCreateFlag.CUBE_COMPATIBLE);
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = VkSampleCount.COUNT_4;
			info.tiling = VkImageTiling.LINEAR;
			info.initialLayout = VkImageLayout.PREINITIALIZED;
			info.usage = new EnumMask<>(props.usage());
			info.sharingMode = props.mode();

			// Check API
			final var reqs = new VkMemoryRequirements() {
				@Override
				public boolean equals(Object obj) {
					return true;
				}
			};
			verify(lib).vkCreateImage(dev, info, null, dev.factory().pointer());
			verify(lib).vkGetImageMemoryRequirements(dev, image.handle(), reqs);
			verify(lib).vkBindImageMemory(dev, image.handle(), mem, 0L);
		}

		@DisplayName("A default image must have memory properties")
		@Test
		void properties() {
			builder.descriptor(descriptor);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev, allocator));
		}

		@DisplayName("A default image must have an image descriptor")
		@Test
		void descriptor() {
			builder.properties(props);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev, allocator));
		}

		@DisplayName("A default image must have a valid initial layout")
		@Test
		void layout() {
			assertThrows(IllegalArgumentException.class, () -> builder.initialLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
		}
	}
}
