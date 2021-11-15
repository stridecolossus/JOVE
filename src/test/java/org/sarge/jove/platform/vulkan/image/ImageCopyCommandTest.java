package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsage;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkOffset3D;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.image.ImageCopyCommand.CopyRegion;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Structure;

public class ImageCopyCommandTest {
	private Image image;
	private VulkanBuffer buffer;
	private VulkanLibrary lib;
	private ImageCopyCommand.Builder builder;
	private Command.Buffer cmd;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Define image
		final var descriptor = new ImageDescriptor.Builder()
				.extents(new ImageExtents(2, 3))
				.format(AbstractVulkanTest.FORMAT)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create data buffer
		buffer = mock(VulkanBuffer.class);

		// Create copy command builder
		builder = new ImageCopyCommand.Builder();

		// Create command buffer
		cmd = mock(Command.Buffer.class);
	}

	@Nested
	class CopyRegionTests {
		private VkOffset3D offset;

		@BeforeEach
		void before() {
			offset = new VkOffset3D() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((Structure) obj);
				}
			};
		}

		@Test
		void constructorInvalidRowLength() {
			assertThrows(IllegalArgumentException.class, () -> new CopyRegion(0, 1, 0, image.descriptor(), offset, image.descriptor().extents()));
			assertThrows(IllegalArgumentException.class, () -> new CopyRegion(0, 0, 1, image.descriptor(), offset, image.descriptor().extents()));
		}

		@Test
		void constructorInvalidImageAspects() {
			final var descriptor = new ImageDescriptor.Builder()
					.extents(new ImageExtents(2, 3))
					.format(AbstractVulkanTest.FORMAT)
					.aspect(VkImageAspect.DEPTH)
					.aspect(VkImageAspect.STENCIL)
					.build();
			assertThrows(IllegalArgumentException.class, () -> new CopyRegion(0, 0, 0, descriptor, offset, descriptor.extents()));
		}

		@Test
		void build() {
			final CopyRegion region = new CopyRegion.Builder()
					.offset(offset)
					.extents(image.descriptor().extents())
					.subresource(image.descriptor())
					.build();
			final CopyRegion expected = new CopyRegion(0, 0, 0, image.descriptor(), offset, image.descriptor().extents());
			assertEquals(expected, region);
		}

		@Test
		void of() {
			final ImageDescriptor descriptor = image.descriptor();
			final CopyRegion expected = new CopyRegion(0, 0, 0, descriptor, offset, descriptor.extents());
			assertEquals(expected, CopyRegion.of(descriptor));
		}
	}

	@Nested
	class BuilderTests {
		@Test
		void copy() {
			// Create command to copy the buffer to the whole of the image
			final ImageCopyCommand copy = builder
					.image(image)
					.buffer(buffer)
					.layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.region(image)
					.build();

			// Check buffer is validated
			verify(buffer).require(VkBufferUsage.TRANSFER_SRC, VkBufferUsage.TRANSFER_DST);

			// Init expected copy descriptor
			final var expected = new VkBufferImageCopy() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((Structure) obj);
				}
			};
			expected.imageSubresource.aspectMask = VkImageAspect.COLOR.value();
			expected.imageSubresource.layerCount = 1;
			expected.imageExtent.depth = 1;
			expected.imageExtent.width = 2;
			expected.imageExtent.height = 3;

			// Perform copy operation
			copy.execute(lib, cmd);
			verify(buffer).require(VkBufferUsage.TRANSFER_SRC);

			// Check API
			verify(lib).vkCmdCopyBufferToImage(cmd, buffer, image, VkImageLayout.TRANSFER_DST_OPTIMAL, 1, new VkBufferImageCopy[]{expected});

			// Perform reverse copy operation
			copy.invert().execute(lib, cmd);
			verify(buffer).require(VkBufferUsage.TRANSFER_DST);
			verify(lib).vkCmdCopyBufferToImage(cmd, buffer, image, VkImageLayout.TRANSFER_DST_OPTIMAL, 1, new VkBufferImageCopy[]{expected});
		}

		@Test
		void buildEmptyBuffer() {
			builder.image(image);
			builder.layout(VkImageLayout.TRANSFER_DST_OPTIMAL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyImage() {
			builder.buffer(buffer);
			builder.layout(VkImageLayout.TRANSFER_DST_OPTIMAL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyImageLayout() {
			builder.image(image);
			builder.buffer(buffer);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyRegions() {
			builder.image(image);
			builder.buffer(buffer);
			builder.layout(VkImageLayout.TRANSFER_DST_OPTIMAL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
