package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsage;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.image.Descriptor.Extents;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class ImageCopyCommandTest {
	private static final Handle IMAGE = new Handle(new Pointer(1));
	private static final Handle BUFFER = new Handle(new Pointer(2));
	private static final Handle COMMAND = new Handle(new Pointer(2));

	private Image image;
	private VulkanBuffer buffer;
	private VulkanLibrary lib;
	private ImageCopyCommand.Builder builder;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Define image
		final var descriptor = new Descriptor.Builder()
				.extents(new Extents(2, 3))
				.format(AbstractVulkanTest.FORMAT)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		image = mock(Image.class);
		when(image.handle()).thenReturn(IMAGE);
		when(image.descriptor()).thenReturn(descriptor);

		// Create data buffer
		buffer = mock(VulkanBuffer.class);
		when(buffer.handle()).thenReturn(BUFFER);

		// Create copy command builder
		builder = new ImageCopyCommand.Builder();
		builder.subresource(SubResource.of(descriptor));
	}

	@Test
	void copyBufferToImage() {
		// Create command to copy the buffer to the image
		final ImageCopyCommand copy = builder
				.image(image)
				.buffer(buffer)
				.layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
				.build();

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
		copy.execute(lib, COMMAND);
		verify(buffer).require(VkBufferUsage.TRANSFER_SRC);

		// Check API
		verify(lib).vkCmdCopyBufferToImage(COMMAND, BUFFER, IMAGE, VkImageLayout.TRANSFER_DST_OPTIMAL, 1, new VkBufferImageCopy[]{expected});
	}

	@Test
	void copyImageToBuffer() {
		// Create command to copy the image to the buffer
		final ImageCopyCommand copy = builder
				.image(image)
				.buffer(buffer)
				.layout(VkImageLayout.TRANSFER_SRC_OPTIMAL)
				.invert()
				.build();

		// Perform copy operation
		copy.execute(lib, COMMAND);
		verify(buffer).require(VkBufferUsage.TRANSFER_DST);
	}

	@Test
	void buildEmptyImage() {
		assertThrows(IllegalArgumentException.class, () -> builder.buffer(buffer).build());
	}

	@Test
	void buildEmptyImageLayout() {
		assertThrows(IllegalArgumentException.class, () -> builder.image(image).buffer(buffer).build());
	}

	@Test
	void buildEmptyBuffer() {
		assertThrows(IllegalArgumentException.class, () -> builder.image(image).build());
	}

	@Test
	void buildInvalidBuffer() {
		doThrow(IllegalStateException.class).when(buffer).require(any());
		builder.image(image).buffer(buffer).layout(VkImageLayout.TRANSFER_DST_OPTIMAL);
		assertThrows(IllegalStateException.class, () -> builder.build());
	}
}
