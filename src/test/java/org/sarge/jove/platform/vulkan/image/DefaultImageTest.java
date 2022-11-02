package org.sarge.jove.platform.vulkan.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkImageAspect.COLOR;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.ptr.PointerByReference;

class DefaultImageTest extends AbstractVulkanTest {
	private static final Dimensions EXTENTS = new Dimensions(3, 4);

	private DefaultImage image;
	private Descriptor descriptor;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		// Create descriptor
		descriptor = new Descriptor.Builder()
				.format(FORMAT)
				.extents(EXTENTS)
				.aspect(COLOR)
				.mipLevels(2)
				.arrayLayers(3)
				.build();

		// Init image memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(1));

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
		verify(lib).vkDestroyImage(dev, image, null);
		verify(mem).destroy();
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
					.build();

			// Init memory allocator
			when(allocator.allocate(isA(VkMemoryRequirements.class), eq(props))).thenReturn(mem);

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
					.build(dev);

			// Check image
			assertNotNull(image);
			assertEquals(descriptor, image.descriptor());
			assertEquals(dev, image.device());
			assertEquals(false, image.isDestroyed());

			// Init expected creation descriptor
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

			// Check API
			final PointerByReference ref = factory.pointer();
			final Handle handle = new Handle(ref);
			verify(lib).vkCreateImage(dev, info, null, ref);
			verify(lib).vkGetImageMemoryRequirements(eq(dev), eq(handle), any(VkMemoryRequirements.class));
			verify(lib).vkBindImageMemory(dev, handle, mem, 0L);
		}

		@DisplayName("A default image must have memory properties")
		@Test
		void properties() {
			builder.descriptor(descriptor);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@DisplayName("A default image must have an image descriptor")
		@Test
		void descriptor() {
			builder.properties(props);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@DisplayName("A default image must have a valid initial layout")
		@Test
		void layout() {
			assertThrows(IllegalArgumentException.class, () -> builder.initialLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
		}
	}
}
