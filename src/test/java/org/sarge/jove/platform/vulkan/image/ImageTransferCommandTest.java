package org.sarge.jove.platform.vulkan.image;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.Mockery;

class ImageTransferCommandTest {
	@Test
	void build() {

		final var mockery = new Mockery(Image.Library.class);
		final var device = new MockLogicalDevice(mockery.proxy());
		final var buffer = new MockVulkanBuffer(device, 42L, VkBufferUsageFlags.TRANSFER_SRC);
		final var image = new MockImage();

		final var transfer = new ImageTransferCommand.Builder()
				.buffer(buffer)
				.image(image)
				.layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
				.build();

		transfer.execute(null);
	}
}
