package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure;

public class BarrierTest {
	private Barrier.Builder builder;
	private Family srcFamily, destFamily;
	private VulkanLibrary lib;
	private Command.Buffer cmd;

	@BeforeEach
	void before() {
		lib = mock(VulkanLibrary.class);
		cmd = new MockCommandBuffer();
		srcFamily = new Family(1, 1, Set.of());
		destFamily = new Family(2, 2, Set.of());
		builder = new Barrier.Builder();
	}

	@DisplayName("A memory barrier...")
	@Nested
	class MemoryTests {
		@DisplayName("can be executed to transition device memory")
		@Test
		void build() {
			// Construct a memory barrier
			final Barrier barrier = builder
					.source(VkPipelineStage.TRANSFER)
					.destination(VkPipelineStage.FRAGMENT_SHADER)
					.dependency(VkDependencyFlag.VIEW_LOCAL)
					.memory()
						.source(VkAccess.TRANSFER_WRITE)
						.destination(VkAccess.SHADER_READ)
						.build()
					.build();

			// Init expected descriptor
			final var expected = new VkMemoryBarrier() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((Structure) obj);
				}
			};
			expected.srcAccessMask = BitMask.of(VkAccess.TRANSFER_WRITE);
			expected.dstAccessMask = BitMask.of(VkAccess.SHADER_READ);

			// Check API
			final var src = BitMask.of(VkPipelineStage.TRANSFER);
			final var dest = BitMask.of(VkPipelineStage.FRAGMENT_SHADER);
			final var flags = BitMask.of(VkDependencyFlag.VIEW_LOCAL);
			barrier.record(lib, cmd);
			verify(lib).vkCmdPipelineBarrier(cmd, src, dest, flags, 1, new VkMemoryBarrier[]{expected}, 0, null, 0, null);
		}
	}

	@DisplayName("A buffer memory barrier...")
	@Nested
	class BufferTests {
		private VulkanBuffer buffer;

		@BeforeEach
		void before() {
			buffer = mock(VulkanBuffer.class);
			when(buffer.length()).thenReturn(3L);
		}

		@DisplayName("can be executed to transition a buffer")
		@Test
		void build() {
			// Construct a buffer memory barrier
			final Barrier barrier = builder
					.source(VkPipelineStage.TRANSFER)
					.destination(VkPipelineStage.FRAGMENT_SHADER)
					.dependency(VkDependencyFlag.VIEW_LOCAL)
					.buffer(buffer)
						.source(VkAccess.TRANSFER_WRITE)
						.destination(VkAccess.SHADER_READ)
						.source(srcFamily)
						.destination(destFamily)
						.offset(1)
						.size(2)
						.build()
					.build();

			// Init expected descriptor
			final var expected = new VkBufferMemoryBarrier() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((Structure) obj);
				}
			};
			expected.srcAccessMask = BitMask.of(VkAccess.TRANSFER_WRITE);
			expected.dstAccessMask = BitMask.of(VkAccess.SHADER_READ);
			expected.srcQueueFamilyIndex = 1;
			expected.dstQueueFamilyIndex = 2;
			expected.offset = 1;
			expected.size = 2;

			// Check API
			final var src = BitMask.of(VkPipelineStage.TRANSFER);
			final var dest = BitMask.of(VkPipelineStage.FRAGMENT_SHADER);
			final var flags = BitMask.of(VkDependencyFlag.VIEW_LOCAL);
			barrier.record(lib, cmd);
			verify(lib).vkCmdPipelineBarrier(cmd, src, dest, flags, 0, null, 1, new VkBufferMemoryBarrier[]{expected}, 0, null);
		}

		@DisplayName("cannot have an offset larger than the buffer")
		@Test
		void offset() {
			assertThrows(IllegalArgumentException.class, () -> builder.buffer(buffer).offset(3));
		}

		@DisplayName("cannot have a size larger than the buffer")
		@Test
		void size() {
			assertThrows(IllegalArgumentException.class, () -> builder.buffer(buffer).size(4));
		}

		@DisplayName("cannot have an offset and size larger than the buffer")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.buffer(buffer).offset(2).size(2).build());
		}
	}

	@DisplayName("An image memory barrier...")
	@Nested
	class ImageTests {
		private Image image;

		@BeforeEach
		void before() {
			// Init image descriptor and sub-resource
			final Descriptor descriptor = new Descriptor.Builder()
					.format(VkFormat.UNDEFINED)
					.extents(new Dimensions(3, 4))
					.aspect(VkImageAspect.COLOR)
					.build();

			// Create an image
			image = mock(Image.class);
			when(image.descriptor()).thenReturn(descriptor);
		}

		@DisplayName("can be executed to transition an image")
		@Test
		void build() {
			// Construct image memory barrier
			final Barrier barrier = builder
					.source(VkPipelineStage.TRANSFER)
					.destination(VkPipelineStage.FRAGMENT_SHADER)
					.dependency(VkDependencyFlag.VIEW_LOCAL)
					.image(image)
						.source(VkAccess.TRANSFER_WRITE)
						.destination(VkAccess.SHADER_READ)
						.source(srcFamily)
						.destination(destFamily)
						.oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
						.newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL)
						.subresource(image.descriptor())
						.build()
					.build();

			// Init expected descriptor
			final var expected = new VkImageMemoryBarrier() {
				@Override
				public boolean equals(Object obj) {
					final var actual = (VkImageMemoryBarrier) obj;
					assertEquals(BitMask.of(VkAccess.TRANSFER_WRITE), actual.srcAccessMask);
					assertEquals(BitMask.of(VkAccess.SHADER_READ), actual.dstAccessMask);
					assertEquals(1, actual.srcQueueFamilyIndex);
					assertEquals(2, actual.dstQueueFamilyIndex);
					assertEquals(VkImageLayout.TRANSFER_DST_OPTIMAL, actual.oldLayout);
					assertEquals(VkImageLayout.SHADER_READ_ONLY_OPTIMAL, actual.newLayout);
					assertNotNull(actual.subresourceRange);
					return true;
				}
			};

			// Check API
			final var src = BitMask.of(VkPipelineStage.TRANSFER);
			final var dest = BitMask.of(VkPipelineStage.FRAGMENT_SHADER);
			final var flags = BitMask.of(VkDependencyFlag.VIEW_LOCAL);
			barrier.record(lib, cmd);
			verify(lib).vkCmdPipelineBarrier(cmd, src, dest, flags, 0, null, 0, null, 1, new VkImageMemoryBarrier[]{expected});
		}

		@DisplayName("must have a new layout configured")
		@Test
		void layout() {
			assertThrows(IllegalArgumentException.class, () -> builder.image(image).build());
		}

		@DisplayName("cannot have the same previous and new layout")
		@Test
		void same() {
			assertThrows(IllegalArgumentException.class, () -> builder.image(image).newLayout(VkImageLayout.UNDEFINED).build());
		}
	}
}
