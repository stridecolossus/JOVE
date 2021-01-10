package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ImageCopyCommandTest {
	private Image image;
	private VulkanBuffer buffer;
	private Handle cmd;
	private VulkanLibrary lib;
	private ImageCopyCommand.Builder builder;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Create command buffer
		cmd = new Handle(new Pointer(1));

		// Define image
		final var descriptor = new Image.Descriptor.Builder()
				.extents(new Image.Extents(1, 1))
				.format(AbstractVulkanTest.FORMAT)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.build();

		// Create image
		image = mock(Image.class);
		when(image.handle()).thenReturn(new Handle(new Pointer(2)));
		when(image.descriptor()).thenReturn(descriptor);

		// Create data buffer
		buffer = mock(VulkanBuffer.class);
		when(buffer.handle()).thenReturn(new Handle(new Pointer(3)));

		// Create copy command builder
		builder = new ImageCopyCommand.Builder();
	}

	@Test
	void copyBufferToImage() {
		// Create command to copy the buffer to the image
		final ImageCopyCommand copy = builder
				.image(image)
				.buffer(buffer)
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.build();

		// Perform copy operation
		copy.execute(lib, cmd);
		verify(buffer).require(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		verify(lib).vkCmdCopyBufferToImage(cmd, buffer.handle(), image.handle(), VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 1, copy.regions());
	}

	@Test
	void copyImageToBuffer() {
		// Create command to copy the image to the buffer
		final ImageCopyCommand copy = builder
				.image(image)
				.buffer(buffer)
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
				.invert()
				.build();

		// Perform copy operation
		copy.execute(lib, cmd);
		verify(buffer).require(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		verify(lib).vkCmdCopyImageToBuffer(cmd, image.handle(), VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, buffer.handle(), 1, copy.regions());
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
	void buildSubresourceEmptyImage() {
		assertThrows(IllegalStateException.class, () -> builder.buffer(buffer).subresource());
	}

	@Test
	void buildEmptyBuffer() {
		assertThrows(IllegalArgumentException.class, () -> builder.image(image).build());
	}

	@Test
	void buildInvalidImageLayout() {
		builder.image(image).buffer(buffer).layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
		assertThrows(IllegalStateException.class, () -> builder.build());
	}

	@Test
	void buildInvalidBuffer() {
		doThrow(IllegalStateException.class).when(buffer).require(any());
		builder.image(image).buffer(buffer).layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
		assertThrows(IllegalStateException.class, () -> builder.build());
	}
}
