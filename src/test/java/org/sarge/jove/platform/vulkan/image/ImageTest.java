package org.sarge.jove.platform.vulkan.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageTiling;
import org.sarge.jove.platform.vulkan.VkImageUsage;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSampleCountFlag;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.image.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

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
	void destroy() {
		image.close();
		verify(lib).vkDestroyImage(dev.handle(), image.handle(), null);
		verify(mem).close();
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
			assertEquals(new Handle(POINTER.getValue()), image.handle());
			assertEquals(descriptor, image.descriptor());
			assertEquals(dev, image.device());
			assertEquals(false, image.isDestroyed());

			// Check create image API
			final VkImageCreateInfo info = new VkImageCreateInfo() {
				@Override
				public boolean equals(Object o) {
					return dataEquals((Structure) o);
				}
			};
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent3D();
			info.mipLevels = descriptor.levels();
			info.arrayLayers = descriptor.layers();
			info.samples = VkSampleCountFlag.COUNT_1;
			info.tiling = VkImageTiling.LINEAR;
			info.initialLayout = VkImageLayout.PREINITIALIZED;
			info.usage = IntegerEnumeration.mask(props.usage());
			info.sharingMode = props.mode();
			verify(lib).vkCreateImage(DEVICE, info, null, POINTER);

			// TODO
			//verify(lib).vkGetImageMemoryRequirements(DEVICE, POINTER.getValue(), new VkMemoryRequirements());

			// Check bind memory API
			verify(lib).vkBindImageMemory(DEVICE, POINTER.getValue(), mem.handle(), 0L);
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

		@Test
		void buildInvalidLayout() {
			assertThrows(IllegalArgumentException.class, () -> builder.initialLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
		}
	}
}
