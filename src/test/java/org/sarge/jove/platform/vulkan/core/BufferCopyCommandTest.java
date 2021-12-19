package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.core.BufferCopyCommand.Builder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class BufferCopyCommandTest extends AbstractVulkanTest {
	private BufferCopyCommand copy;
	private VulkanBuffer src, dest;

	@BeforeEach
	void before() {
		// Create buffers
		src = mock(VulkanBuffer.class);
		dest = mock(VulkanBuffer.class);

		// Init sizes
		when(src.length()).thenReturn(2L);
		when(dest.length()).thenReturn(3L);

		// Create copy command
		copy = new Builder()
				.source(src)
				.destination(dest)
				.region(0, 1, 2)
				.build();
	}

	@Test
	void constructor() {
		verify(src).require(VkBufferUsageFlag.TRANSFER_SRC);
		verify(dest).require(VkBufferUsageFlag.TRANSFER_DST);
	}

	@Test
	void execute() {
		// Execute copy
		final Command.Buffer cmd = mock(Command.Buffer.class);
		copy.execute(lib, cmd);

		// Check API
		final VkBufferCopy region = new VkBufferCopy() {
			@Override
			public boolean equals(Object obj) {
				return
						(obj instanceof VkBufferCopy that) &&
						(that.srcOffset == 0) &&
						(that.dstOffset == 1) &&
						(that.size == 2);
			}
		};
		verify(lib).vkCmdCopyBuffer(cmd, src, dest, 1, new VkBufferCopy[]{region});
	}

	@Test
	void of() {
		assertNotNull(BufferCopyCommand.of(src, dest));
	}

	@Test
	void invert() {
		final Command inverse = copy.invert();
		assertNotNull(inverse);
		verify(src).require(VkBufferUsageFlag.TRANSFER_DST);
		verify(dest).require(VkBufferUsageFlag.TRANSFER_SRC);
		inverse.execute(lib, mock(Command.Buffer.class));
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
			// Build copy command
			builder
					.source(src)
					.destination(dest)
					.region(0, 0, 1);

			// Check command
			assertNotNull(builder.build());

			// Check buffers
			verify(src, times(2)).require(VkBufferUsageFlag.TRANSFER_SRC);
			verify(dest, times(2)).require(VkBufferUsageFlag.TRANSFER_DST);
		}

		@Test
		void buildMissingSource() {
			builder.destination(dest);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildMissingDestination() {
			builder.source(src);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildCopySelf() {
			builder.source(src);
			builder.destination(src);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyCopyRegions() {
			builder.source(src);
			builder.destination(dest);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void regionInvalidSourceSize() {
			builder.source(src);
			builder.destination(dest);
			assertThrows(IllegalArgumentException.class, () -> builder.region(0, 0, 3));
			assertThrows(IllegalArgumentException.class, () -> builder.region(1, 0, 2));
		}

		@Test
		void regionDestinationTooSmall() {
			builder.source(src);
			builder.destination(dest);
			when(src.length()).thenReturn(999L);
			assertThrows(IllegalArgumentException.class, () -> builder.region(0, 0, 4));
			assertThrows(IllegalArgumentException.class, () -> builder.region(0, 1, 3));
		}
	}
}
