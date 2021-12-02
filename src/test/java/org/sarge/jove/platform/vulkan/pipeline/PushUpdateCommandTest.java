package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.BufferWrapper;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.PushConstantRange;
import org.sarge.jove.platform.vulkan.pipeline.PushUpdateCommand.Builder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;

class PushUpdateCommandTest extends AbstractVulkanTest {
	private static final Set<VkShaderStage> STAGES = Set.of(VkShaderStage.VERTEX, VkShaderStage.FRAGMENT);

	private PushUpdateCommand update;
	private PipelineLayout layout;
	private ByteBuffer data;

	@BeforeEach
	void before() {
		data = BufferWrapper.allocate(4);
		layout = new PipelineLayout(new Pointer(1), dev, 8, STAGES);
		update = new PushUpdateCommand(layout, 4, data, STAGES);
	}

	@Test
	void constructor() {
		assertEquals(data, update.data());
	}

	@Test
	void data() {
		final ByteBuffer bb = PushUpdateCommand.data(layout);
		assertNotNull(bb);
		assertEquals(8, bb.capacity());
	}

	@Test
	void of() {
		final ByteBuffer bb = BufferWrapper.allocate(8);
		final PushUpdateCommand expected = new PushUpdateCommand(layout, 0, bb, STAGES);
		assertEquals(expected, PushUpdateCommand.of(layout));
	}

	@Test
	void execute() {
		final int stages = IntegerEnumeration.mask(STAGES);
		final Command.Buffer cmd = mock(Command.Buffer.class);
		update.execute(lib, cmd);
		verify(lib).vkCmdPushConstants(cmd, layout, stages, 4, 4, data);
	}

	@Test
	void constructorInvalidOffsetAlignment() {
		assertThrows(IllegalArgumentException.class, () -> new PushUpdateCommand(layout, 1, data, STAGES));
	}

	@Test
	void constructorInvalidDataBufferAlignment() {
		assertThrows(IllegalArgumentException.class, () -> new PushUpdateCommand(layout, 0, BufferWrapper.allocate(1), STAGES));
	}

	@Test
	void constructorEmptyDataBuffer() {
		assertThrows(IllegalArgumentException.class, () -> new PushUpdateCommand(layout, 0, BufferWrapper.allocate(0), STAGES));
	}

	@Test
	void constructorInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new PushUpdateCommand(layout, 8, data, STAGES));
	}

	@Test
	void constructorEmptyStages() {
		assertThrows(IllegalArgumentException.class, () -> new PushUpdateCommand(layout, 0, data, Set.of()));
	}

	@Test
	void constructorInvalidPipelineStage() {
		assertThrows(IllegalArgumentException.class, () -> new PushUpdateCommand(layout, 0, data, Set.of(VkShaderStage.GEOMETRY)));
	}

	@Nested
	class BuilderTests {
		private Builder builder;
		private ByteBuffer whole;

		@BeforeEach
		void before() {
			builder = new Builder();
			whole = BufferWrapper.allocate(8);
		}

		@Test
		void build() {
			final PushUpdateCommand result = builder
					.offset(4)
					.data(data)
					.stage(VkShaderStage.VERTEX)
					.stage(VkShaderStage.FRAGMENT)
					.build(layout);

			assertEquals(update, result);
		}

		@Test
		void buildEmptyBuffer() {
			assertThrows(IllegalArgumentException.class, () -> builder.build(layout));
		}

		@Test
		void slice() {
			// Create command to update a slice of the buffer
			final PushUpdateCommand result = builder
					.data(whole, 4, 4)
					.stage(VkShaderStage.FRAGMENT)
					.build(layout);

			// Check sliced command
			final ByteBuffer slice = whole.slice(4, 4);
			final PushUpdateCommand expected = new PushUpdateCommand(layout, 0, slice, Set.of(VkShaderStage.FRAGMENT));
			assertEquals(expected, result);
		}

		@Test
		void range() {
			// Create a push constant range
			final PushConstantRange range = new PushConstantRange(4, 4, Set.of(VkShaderStage.FRAGMENT));

			// Create a command to update the slice referenced by the range
			final PushUpdateCommand result = builder
					.data(whole, range)
					.stage(VkShaderStage.FRAGMENT)
					.build(layout);

			// Check sliced command
			final ByteBuffer slice = whole.slice(4, 4);
			final PushUpdateCommand expected = new PushUpdateCommand(layout, 0, slice, Set.of(VkShaderStage.FRAGMENT));
			assertEquals(expected, result);
		}
	}
}
