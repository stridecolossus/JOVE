package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.BufferCopyCommand.Builder;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;

public class BufferCopyCommandTest {
	private BufferCopyCommand copy;
	private VulkanBuffer src, dest;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();

		// Create buffers
		final var usage = Set.of(VkBufferUsageFlag.TRANSFER_SRC, VkBufferUsageFlag.TRANSFER_DST);
		final var mem = mock(DeviceMemory.class);
		src = new VulkanBuffer(new Handle(1), dev, usage, mem, 1);
		dest = new VulkanBuffer(new Handle(2), dev, usage, mem, 2);

		// Create copy command
		copy = BufferCopyCommand.of(src, dest);
	}

	@Test
	void execute() {
		// Execute copy
		final var cmd = new MockCommandBuffer();
		copy.record(dev.library(), cmd);

		// Check API
		final VkBufferCopy region = new VkBufferCopy() {
			@Override
			public boolean equals(Object obj) {
				return
						(obj instanceof VkBufferCopy that) &&
						(that.srcOffset == 0) &&
						(that.dstOffset == 0) &&
						(that.size == 1);
			}
		};
		verify(dev.library()).vkCmdCopyBuffer(cmd, src, dest, 1, new VkBufferCopy[]{region});
	}

	@Test
	void invert() {
		copy.invert().record(dev.library(), null);
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
