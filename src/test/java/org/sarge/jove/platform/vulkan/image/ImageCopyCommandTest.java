package org.sarge.jove.platform.vulkan.image;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.Image.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ImageCopyCommandTest extends AbstractVulkanTest {
	@Test
	void copy() {
		final Extents extents = new Extents(new Dimensions(2, 3));
		final Descriptor descriptor = new Descriptor.Builder()
				.format(FORMAT)
				.aspect(VkImageAspect.COLOR)
				.extents(extents)
				.build();
		final Image src = mock(Image.class);
		final Image dest = mock(Image.class);
		when(src.descriptor()).thenReturn(descriptor);
		when(dest.descriptor()).thenReturn(descriptor);

		final ImageCopyCommand copy = ImageCopyCommand.of(src, dest);
		final Buffer buffer = mock(Buffer.class);
		final var region = new VkImageCopy() {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		copy.record(lib, buffer);
		verify(lib).vkCmdCopyImage(buffer, src, VkImageLayout.TRANSFER_SRC_OPTIMAL, dest, VkImageLayout.TRANSFER_DST_OPTIMAL, 1, new VkImageCopy[]{region});
	}
}
