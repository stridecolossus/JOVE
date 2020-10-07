package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkOffset3D;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Pointer;

public class ImageCopyCommandTest {
	private static final VkImageLayout LAYOUT = VkImageLayout.VK_IMAGE_LAYOUT_GENERAL;

	private Image image;
	private VertexBuffer buffer;
	private VkBufferImageCopy region;
	private Handle handle;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Create command buffer
		handle = new Handle(new Pointer(1));

		// Create image
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.handle(new Handle(new Pointer(2)))
				.extents(new Image.Extents(3, 4))
				.format(VkFormat.VK_FORMAT_A1R5G5B5_UNORM_PACK16)
				.build();
		image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create data buffer
		buffer = mock(VertexBuffer.class);
		when(buffer.handle()).thenReturn(new Handle(new Pointer(5)));

		// Create copy descriptor
		region = new VkBufferImageCopy();
	}

	@Test
	void copyImageToBuffer() {
		final VkBufferImageCopy[] array = new VkBufferImageCopy[]{region};
		final ImageCopyCommand copy = new ImageCopyCommand(image, buffer, array, LAYOUT);
		copy.execute(lib, handle);
		verify(lib).vkCmdCopyBufferToImage(handle, buffer.handle(), image.handle(), LAYOUT, 1, array);
	}

	@Test
	void copyBufferToImage() {
		final VkBufferImageCopy[] array = new VkBufferImageCopy[]{region};
		final ImageCopyCommand copy = new ImageCopyCommand(image, buffer, array, LAYOUT);
		final Command inverse = copy.invert();
		assertNotNull(inverse);
		inverse.execute(lib, handle);
		verify(lib).vkCmdCopyImageToBuffer(handle, image.handle(), LAYOUT, buffer.handle(), 1, array);

	}

	@Nested
	class BuilderTests {
		private ImageCopyCommand.Builder builder;

		@BeforeEach
		void before() {
			builder = new ImageCopyCommand.Builder();
		}

		@Test
		void build() {
			// Build copy command
			final Command copy = builder
					.image(image)
					.buffer(buffer)
					.layout(LAYOUT)
					.subresource()
						.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)		// TODO - init from image?
						.build()
					.build();

			// Copy
			assertNotNull(copy);
			copy.execute(lib, handle);

			// Check API
			final ArgumentCaptor<VkBufferImageCopy[]> captor = ArgumentCaptor.forClass(VkBufferImageCopy[].class);
			final Handle bufferHandle = buffer.handle();
			final Handle imageHandle = image.handle();
			verify(lib).vkCmdCopyBufferToImage(eq(handle), eq(bufferHandle), eq(imageHandle), eq(LAYOUT), eq(1), captor.capture());
			assertNotNull(captor.getValue());
			assertEquals(1, captor.getValue().length);

			// Check descriptor
			final VkBufferImageCopy info = captor.getValue()[0];
			assertEquals(0, info.bufferOffset);
			assertEquals(0, info.bufferRowLength);
			assertEquals(0, info.bufferImageHeight);

			// Check image offsets
			assertNotNull(info.imageOffset);
			assertTrue(info.imageOffset.dataEquals(new VkOffset3D()));

			// Check image extents
			assertNotNull(info.imageExtent);
			assertTrue(info.imageExtent.dataEquals(image.descriptor().extents().create()));

			// Check sub-resource
			assertNotNull(info.imageSubresource);
			assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), info.imageSubresource.aspectMask);
		}

		@Test
		void buildRequiresImage() {
			builder.buffer(buffer).layout(LAYOUT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildRequiresBuffer() {
			builder.image(image).layout(LAYOUT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildRequiresImageLayout() {
			builder.image(image).buffer(buffer);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
