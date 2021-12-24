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
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.PushConstantUpdateCommand.Builder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.BufferHelper;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;

class PushConstantUpdateCommandTest extends AbstractVulkanTest {
	private static final Set<VkShaderStage> STAGES = Set.of(VkShaderStage.VERTEX, VkShaderStage.FRAGMENT);

	private PushConstantUpdateCommand update;
	private PipelineLayout layout;
	private ByteBuffer data;

	@BeforeEach
	void before() {
		data = BufferHelper.allocate(4);
		layout = new PipelineLayout(new Pointer(1), dev, 8, STAGES);
		update = new PushConstantUpdateCommand(layout, 4, data, STAGES);
	}

	@Test
	void constructor() {
		assertEquals(data, update.data());
	}

	@Test
	void data() {
		final ByteBuffer bb = PushConstantUpdateCommand.data(layout);
		assertNotNull(bb);
		assertEquals(8, bb.capacity());
	}

	@Test
	void of() {
		final ByteBuffer bb = BufferHelper.allocate(8);
		final PushConstantUpdateCommand expected = new PushConstantUpdateCommand(layout, 0, bb, STAGES);
		assertEquals(expected, PushConstantUpdateCommand.of(layout));
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
		assertThrows(IllegalArgumentException.class, () -> new PushConstantUpdateCommand(layout, 1, data, STAGES));
	}

	@Test
	void constructorInvalidDataBufferAlignment() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantUpdateCommand(layout, 0, BufferHelper.allocate(1), STAGES));
	}

	@Test
	void constructorEmptyDataBuffer() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantUpdateCommand(layout, 0, BufferHelper.allocate(0), STAGES));
	}

	@Test
	void constructorInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantUpdateCommand(layout, 8, data, STAGES));
	}

	@Test
	void constructorEmptyStages() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantUpdateCommand(layout, 0, data, Set.of()));
	}

	@Test
	void constructorInvalidPipelineStage() {
		assertThrows(IllegalArgumentException.class, () -> new PushConstantUpdateCommand(layout, 0, data, Set.of(VkShaderStage.GEOMETRY)));
	}

	@Nested
	class BuilderTests {
		private Builder builder;
		private ByteBuffer whole;

		@BeforeEach
		void before() {
			builder = new Builder();
			whole = BufferHelper.allocate(8);
		}

		@Test
		void build() {
			final PushConstantUpdateCommand result = builder
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
			final PushConstantUpdateCommand result = builder
					.data(whole, 4, 4)
					.stage(VkShaderStage.FRAGMENT)
					.build(layout);

			// Check sliced command
			final ByteBuffer slice = whole.slice(4, 4);
			final PushConstantUpdateCommand expected = new PushConstantUpdateCommand(layout, 0, slice, Set.of(VkShaderStage.FRAGMENT));
			assertEquals(expected, result);
		}

		@Test
		void range() {
			// Create a push constant range
			final PushConstantRange range = new PushConstantRange(4, 4, Set.of(VkShaderStage.FRAGMENT));

			// Create a command to update the slice referenced by the range
			final PushConstantUpdateCommand result = builder
					.data(whole, range)
					.stage(VkShaderStage.FRAGMENT)
					.build(layout);

			// Check sliced command
			final ByteBuffer slice = whole.slice(4, 4);
			final PushConstantUpdateCommand expected = new PushConstantUpdateCommand(layout, 0, slice, Set.of(VkShaderStage.FRAGMENT));
			assertEquals(expected, result);
		}
	}
}
