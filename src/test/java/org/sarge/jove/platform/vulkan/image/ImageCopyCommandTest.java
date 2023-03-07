package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.image.Image.*;

public class ImageCopyCommandTest {
	private Image src, dest;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		lib = dev.library();

		final Extents extents = new Extents(new Dimensions(2, 3));
		final Descriptor descriptor = new Descriptor.Builder()
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.aspect(VkImageAspect.COLOR)
				.extents(extents)
				.build();

		src = mock(Image.class);
		dest = mock(Image.class);
		when(src.descriptor()).thenReturn(descriptor);
		when(dest.descriptor()).thenReturn(descriptor);
	}

	@Test
	void copy() {
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

	@Nested
	class BuilderTests {
		private ImageCopyCommand.Builder builder;

		@BeforeEach
		void before() {
			builder = new ImageCopyCommand.Builder(src, dest);
		}

		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void self() {
			assertThrows(IllegalArgumentException.class, () -> new ImageCopyCommand.Builder(src, src));
		}

    	@ParameterizedTest
    	@EnumSource(value=VkImageLayout.class, names={"TRANSFER_SRC_OPTIMAL", "GENERAL", "SHARED_PRESENT_KHR"})
    	void source(VkImageLayout layout) {
			builder.source(layout);
    	}

    	@ParameterizedTest
    	@EnumSource(value=VkImageLayout.class, names={"TRANSFER_DST_OPTIMAL", "GENERAL", "SHARED_PRESENT_KHR"})
    	void destination(VkImageLayout layout) {
			builder.destination(layout);
    	}

    	@Test
    	void invalid() {
    		assertThrows(IllegalArgumentException.class, () -> builder.source(VkImageLayout.PREINITIALIZED));
    		assertThrows(IllegalArgumentException.class, () -> builder.destination(VkImageLayout.PREINITIALIZED));
    	}
    }
}
