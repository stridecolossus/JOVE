package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.Image.Extents;
import org.sarge.jove.platform.vulkan.image.ImageTransferCommand.CopyRegion;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;

public class ImageTransferCommandTest {
	private static final VkBufferImageCopy[] REGIONS = new VkBufferImageCopy[0];

	private Image image;
	private Command.Buffer cmd;
	private VulkanBuffer src, dest;
	private Image.Descriptor descriptor;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		lib = dev.library();

		image = mock(Image.class);
		cmd = new MockCommandBuffer();

		src = create(dev, VkBufferUsageFlag.TRANSFER_SRC);
		dest = create(dev, VkBufferUsageFlag.TRANSFER_DST);

		descriptor = new Image.Descriptor.Builder()
				.aspect(VkImageAspect.COLOR)
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.extents(new Dimensions(2, 3))
				.build();
	}

	private static VulkanBuffer create(DeviceContext dev, VkBufferUsageFlag usage) {
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(usage)
				.required(VkMemoryProperty.DEVICE_LOCAL)
				.build();

		return VulkanBuffer.create(dev, null, 4, props); // TODO - service
	}

	@DisplayName("A transfer command with a source buffer...")
	@Nested
	class BufferToImage {
		@DisplayName("can copy to an image with a valid layout")
		@ParameterizedTest
		@EnumSource(names={"GENERAL", "SHARED_PRESENT_KHR", "TRANSFER_DST_OPTIMAL"})
		void copyBufferImage(VkImageLayout layout) {
			final var copy = new ImageTransferCommand(image, src, true, REGIONS, layout);
			copy.record(lib, cmd);
			verify(lib).vkCmdCopyBufferToImage(cmd, src, image, layout, 0, REGIONS);
		}

		@DisplayName("cannot copy to an image with an invalid layout")
		@Test
		void layout() {
			assertThrows(IllegalStateException.class, () -> new ImageTransferCommand(image, src, true, REGIONS, VkImageLayout.PREINITIALIZED));
		}

		@DisplayName("cannot copy from a buffer that is not a source")
		@Test
		void source() {
			assertThrows(IllegalStateException.class, () -> new ImageTransferCommand(image, dest, true, REGIONS, VkImageLayout.GENERAL));
		}
	}

	@DisplayName("A transfer command with a destination buffer...")
	@Nested
	class ImageToBuffer {
		@DisplayName("can copy from an image with a valid layout")
		@ParameterizedTest
		@EnumSource(names={"GENERAL", "SHARED_PRESENT_KHR", "TRANSFER_SRC_OPTIMAL"})
		void copyBufferImage(VkImageLayout layout) {
			final var copy = new ImageTransferCommand(image, dest, false, REGIONS, layout);
			copy.record(lib, cmd);
			verify(lib).vkCmdCopyImageToBuffer(cmd, image, layout, dest, 0, new VkBufferImageCopy[0]);
		}

		@DisplayName("cannot copy from an image with an invalid layout")
		@Test
		void layout() {
			assertThrows(IllegalStateException.class, () -> new ImageTransferCommand(image, dest, false, REGIONS, VkImageLayout.PREINITIALIZED));
		}

		@DisplayName("cannot copy to a buffer that is not a destination")
		@Test
		void source() {
			assertThrows(IllegalStateException.class, () -> new ImageTransferCommand(image, src, false, REGIONS, VkImageLayout.GENERAL));
		}
	}

	@Nested
	class InvertTests {
		private VulkanBuffer buffer;

		@BeforeEach
		void before() {
			final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
					.usage(VkBufferUsageFlag.TRANSFER_SRC)
					.usage(VkBufferUsageFlag.TRANSFER_DST)
					.required(VkMemoryProperty.DEVICE_LOCAL)
					.build();

			buffer = VulkanBuffer.create(dev, null, 4, props);
		}

		@DisplayName("A transfer command can be inverted")
    	@Test
    	void invert() {
    		final var copy = new ImageTransferCommand(image, buffer, true, REGIONS, VkImageLayout.GENERAL);
    		copy.invert();
    	}

    	@DisplayName("A transfer command cannot be inverted if the buffer does not support both directions")
    	@Test
    	void buffer() {
    		final var copy = new ImageTransferCommand(image, src, true, REGIONS, VkImageLayout.GENERAL);
    		assertThrows(IllegalStateException.class, () -> copy.invert());
    	}

    	@DisplayName("A transfer command cannot be inverted if the image layout does not support both directions")
    	@Test
    	void layout() {
    		final var copy = new ImageTransferCommand(image, buffer, true, REGIONS, VkImageLayout.TRANSFER_DST_OPTIMAL);
    		assertThrows(IllegalStateException.class, () -> copy.invert());
    	}
	}

	@DisplayName("A region of a transfer command...")
	@Nested
	class CopyRegionTests {
		@Test
		void constructor() {
			final CopyRegion region = new CopyRegion(1, new Dimensions(0, 0), descriptor, Extents.ZERO, descriptor.extents());
			assertEquals(1L, region.offset());
			assertEquals(new Dimensions(0, 0), region.row());
			assertEquals(descriptor, region.subresource());
			assertEquals(Extents.ZERO, region.imageOffset());
			assertEquals(descriptor.extents(), region.extents());
		}

		@DisplayName("can be constructed for the whole of the image")
		@Test
		void of() {
			final CopyRegion region = CopyRegion.of(descriptor);
			assertEquals(0L, region.offset());
			assertEquals(new Dimensions(0, 0), region.row());
			assertEquals(descriptor, region.subresource());
			assertEquals(Extents.ZERO, region.imageOffset());
			assertEquals(descriptor.extents(), region.extents());
		}

		@DisplayName("cannot be applied to an image with multiple aspects")
		@Test
		void multiple() {
			descriptor = new Image.Descriptor.Builder()
					.aspect(VkImageAspect.DEPTH)
					.aspect(VkImageAspect.STENCIL)
					.format(VkFormat.R32G32B32A32_SFLOAT)
					.extents(new Dimensions(2, 3))
					.build();

			assertThrows(IllegalArgumentException.class, () -> new CopyRegion(1, new Dimensions(0, 0), descriptor, Extents.ZERO, descriptor.extents()));
		}

		@DisplayName("cannot have a non-zero row length/height smaller than the image extents")
		@Test
		void row() {
			assertThrows(IllegalArgumentException.class, () -> new CopyRegion(1, new Dimensions(1, 2), descriptor, Extents.ZERO, descriptor.extents()));
		}

		@DisplayName("can be converted to the Vulkan descriptor")
		@Test
		void populate() {
			final CopyRegion region = new CopyRegion(1, new Dimensions(0, 0), descriptor, Extents.ZERO, descriptor.extents());
			final var copy = new VkBufferImageCopy();
			region.populate(copy);
			assertEquals(0, copy.bufferRowLength);
			assertEquals(0, copy.bufferImageHeight);
			assertNotNull(copy.imageSubresource);
			assertEquals(0, copy.imageOffset.x);
			assertEquals(0, copy.imageOffset.y);
			assertEquals(0, copy.imageOffset.z);
			assertEquals(1, copy.imageExtent.depth);
			assertEquals(2, copy.imageExtent.width);
			assertEquals(3, copy.imageExtent.height);
		}
	}

	@DisplayName("A transfer command created via the builder...")
	@Nested
	class BuilderTests {
		private ImageTransferCommand.Builder builder;

		@BeforeEach
		void before() {
			builder = new ImageTransferCommand.Builder();
		}

		@DisplayName("can copy a buffer to an image")
		@Test
		void build() {
			builder
					.buffer(src)
					.image(image)
					.layout(VkImageLayout.GENERAL)
					.region(CopyRegion.of(descriptor))
					.build();
		}

		@DisplayName("can be inverted to copy an image to a buffer")
		@Test
		void invert() {
			builder
					.buffer(dest)
					.image(image)
					.layout(VkImageLayout.GENERAL)
					.region(CopyRegion.of(descriptor))
					.invert()
					.build();
		}

		@DisplayName("must have an image configured")
		@Test
		void image() {
			builder.buffer(dest);
			builder.layout(VkImageLayout.GENERAL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("must have a buffer configured")
		@Test
		void buffer() {
			builder.image(image);
			builder.layout(VkImageLayout.GENERAL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("must have an image layout configured")
		@Test
		void layout() {
			builder.image(image);
			builder.buffer(dest);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("must have at least one copy region")
		@Test
		void empty() {
			builder.buffer(dest);
			builder.image(image);
			builder.layout(VkImageLayout.GENERAL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
