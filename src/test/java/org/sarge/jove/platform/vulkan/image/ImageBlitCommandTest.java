package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.ImageBlitCommand.Builder;
import org.sarge.jove.platform.vulkan.image.ImageBlitCommand.Builder.BlitRegion;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor.Extents;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ImageBlitCommandTest extends AbstractVulkanTest {
	private ImageBlitCommand blit;
	private VkImageBlit region;
	private Image image;

	@BeforeEach
	void before() {
		image = mock(Image.class);
		region = new VkImageBlit();
		blit = new ImageBlitCommand(image, VkImageLayout.TRANSFER_SRC_OPTIMAL, image, VkImageLayout.TRANSFER_DST_OPTIMAL, new VkImageBlit[]{region}, VkFilter.LINEAR);
	}

	@Test
	void execute() {
		final var buffer = mock(Command.Buffer.class);
		blit.execute(lib, buffer);
		verify(lib).vkCmdBlitImage(buffer, image, VkImageLayout.TRANSFER_SRC_OPTIMAL, image, VkImageLayout.TRANSFER_DST_OPTIMAL, 1, new VkImageBlit[]{region}, VkFilter.LINEAR);
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			// Init copy regions
			final SubResource res = mock(SubResource.class);
			final Extents offset = new Extents(new Dimensions(2, 3));
			final BlitRegion src = new BlitRegion(res, BlitRegion.MIN_OFFSET, offset);
			final BlitRegion dest = new BlitRegion(res, BlitRegion.MIN_OFFSET, offset);

			// Build blit command
			blit = builder
					.source(image)
					.destination(image)
					.region(src, dest)
					.filter(VkFilter.NEAREST)
					.build();

			// Init expected blit descriptor
			final VkImageBlit expected = new VkImageBlit() {
				@Override
				public boolean equals(Object obj) {
					final VkImageBlit blit = (VkImageBlit) obj;
					assertNotNull(blit);
					assertNotNull(blit.srcSubresource);
					assertNotNull(blit.dstSubresource);
					check(blit.srcOffsets);
					check(blit.dstOffsets);
					return true;
				}

				private void check(VkOffset3D[] offsets) {
					assertNotNull(offsets);
					assertEquals(2, offsets.length);
					assertEquals(0, offsets[0].x);
					assertEquals(0, offsets[0].y);
					assertEquals(0, offsets[0].z);
					assertEquals(2, offsets[1].x);
					assertEquals(3, offsets[1].y);
					assertEquals(1, offsets[1].z);
				}
			};

			// Check command
			final var buffer = mock(Command.Buffer.class);
			assertNotNull(blit);
			blit.execute(lib, buffer);
			verify(lib).vkCmdBlitImage(buffer, image, VkImageLayout.TRANSFER_SRC_OPTIMAL, image, VkImageLayout.TRANSFER_DST_OPTIMAL, 1, new VkImageBlit[]{expected}, VkFilter.NEAREST);
		}

		@Test
		void emptySourceImage() {
			builder.destination(image);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void emptyDestinationImage() {
			builder.source(image);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void emptyRegions() {
			builder.source(image);
			builder.destination(image);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
