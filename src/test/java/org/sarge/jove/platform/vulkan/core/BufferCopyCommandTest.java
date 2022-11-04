package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.BufferCopyCommand.Builder;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class BufferCopyCommandTest extends AbstractVulkanTest {
	private BufferCopyCommand copy;
	private VulkanBuffer src, dest;

	@BeforeEach
	void before() {
		// Create buffers
		final var usage = Set.of(VkBufferUsageFlag.TRANSFER_SRC, VkBufferUsageFlag.TRANSFER_DST);
		final var mem = mock(DeviceMemory.class);
		src = VulkanBufferTest.create(dev, usage, mem, 2);
		dest = VulkanBufferTest.create(dev, usage, mem, 3);

		// Create copy command
		copy = new Builder()
				.source(src)
				.destination(dest)
				.region(0, 1, 2)
				.build();
	}

	@Test
	void execute() {
		// Execute copy
		final Command.Buffer cmd = mock(Command.Buffer.class);
		copy.record(lib, cmd);

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
		inverse.record(lib, mock(Command.Buffer.class));
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
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
	}
}
