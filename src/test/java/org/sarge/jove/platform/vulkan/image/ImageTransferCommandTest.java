package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.ImageTransferCommand.CopyRegion;
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

	@DisplayName("The target image of the transfer command must be a destination")
	@Test
	void layout() {
//		final boolean valid = switch(layout) {
//		case GENERAL, SHARED_PRESENT_KHR -> true;
//		case TRANSFER_DST_OPTIMAL -> write;
//		case TRANSFER_SRC_OPTIMAL -> !write;
//		default -> false;
//	};
	}

	@DisplayName("An image transfer command can be inverted to copy an image to the buffer")
	@Test
	void invert() {
	}

	@Nested
	class CopyRegionTest {
		@DisplayName("A copy region must have a single image aspect")
		@Test
		void aspect() {
			final var subresource = new Subresource.Builder()
					.aspect(VkImageAspectFlags.COLOR)
					.aspect(VkImageAspectFlags.DEPTH)
					.build();

			final var builder = new CopyRegion.Builder()
					.subresource(subresource)
					.extents(new Extents(new Dimensions(2, 3)));

			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("The dimensions of a copy region cannot be smaller than the image extents")
		@Test
		void dimensions() {
		}
	}
}
