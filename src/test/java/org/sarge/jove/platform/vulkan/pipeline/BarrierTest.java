package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkAccessFlag;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkPipelineStageFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.Queue;

import com.sun.jna.Pointer;

public class BarrierTest {
	private static final VkPipelineStageFlag STAGE = VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;

	private Barrier barrier;
	private VulkanLibrary lib;
	private Handle handle;

	@BeforeEach
	void before() {
		barrier = null;
		lib = mock(VulkanLibrary.class);
		handle = new Handle(new Pointer(1));
	}

	@Nested
	class BuilderTests {
		private Barrier.Builder builder;
		private Image image;
		private Image.Descriptor descriptor;

		@BeforeEach
		void before() {
			builder = new Barrier.Builder();
			descriptor = new Image.Descriptor.Builder().format(VkFormat.VK_FORMAT_B8G8R8A8_UNORM).extents(new Image.Extents(3, 4)).build();
			image = mock(Image.class);
			when(image.descriptor()).thenReturn(descriptor);
		}

		@Test
		void buildEmptyBarrier() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildRequiresNewLayout() {
			final var nested = builder.barrier(image);
			assertThrows(IllegalArgumentException.class, () -> nested.build());
		}

		@Test
		void buildSameLayout() {
			final var nested = builder
					.barrier(image)
					.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

			assertThrows(IllegalArgumentException.class, () -> nested.build());
		}

		@Test
		void build() {
			// Create a barrier
			barrier = builder
					.source(STAGE)
					.destination(STAGE)
					.barrier(image)
						.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
						.source(VkAccessFlag.VK_ACCESS_MEMORY_READ_BIT)
						.destination(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
						.subresource()
							.levelCount(42)
							.build()
						.build()
					.build();

			// Execute barrier
			assertNotNull(barrier);
			barrier.execute(lib, handle);

			// Check API
			final ArgumentCaptor<VkImageMemoryBarrier[]> captor = ArgumentCaptor.forClass(VkImageMemoryBarrier[].class);
			verify(lib).vkCmdPipelineBarrier(eq(handle), eq(STAGE.value()), eq(STAGE.value()), eq(0), eq(0), isNull(), eq(0), isNull(), eq(1), captor.capture());

			// Check image barriers
			assertNotNull(captor.getValue());
			assertEquals(1, captor.getValue().length);

			// Check image barrier descriptor
			final VkImageMemoryBarrier info = captor.getValue()[0];
			assertNotNull(info);
			assertEquals(image.handle(), info.image);
			assertEquals(VkAccessFlag.VK_ACCESS_MEMORY_READ_BIT.value(), info.srcAccessMask);
			assertEquals(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT.value(), info.dstAccessMask);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, info.oldLayout);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, info.newLayout);
			assertEquals(Queue.Family.IGNORED, info.srcQueueFamilyIndex);
			assertEquals(Queue.Family.IGNORED, info.dstQueueFamilyIndex);
			assertNotNull(info.subresourceRange);
		}
	}
}
