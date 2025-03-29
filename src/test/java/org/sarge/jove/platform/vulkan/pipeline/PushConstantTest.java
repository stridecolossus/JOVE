package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkShaderStage.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.util.EnumMask;

public class PushConstantTest {
	private PushConstant constant;
	private Range one, two;

	@BeforeEach
	void before() {
		one = new Range(0, 4, Set.of(VERTEX));
		two = new Range(4, 8, Set.of(FRAGMENT));
		constant = new PushConstant(List.of(one, two));
	}

	@Test
	void constructor() {
		assertEquals(4 + 8, constant.length());
		assertEquals(List.of(one, two), constant.ranges());
	}

	@DisplayName("A push constant has a backing data buffer")
	@Test
	void buffer() {
		final ByteBuffer buffer = constant.buffer();
		assertEquals(4 + 8, buffer.capacity());
	}

	@DisplayName("A push constant cannot contain a range with duplicated shader stages")
	@Test
	void duplicate() {
		final Range dup = new Range(4, 8, Set.of(VERTEX));
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, dup)));
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, one)));
	}

	@DisplayName("A push constant must have a range covering every portion of the buffer")
	@Test
	void coverage() {
		final Range three = new Range(8, 4, Set.of(FRAGMENT));
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, three)));
	}

	@Test
	void equals() {
		assertEquals(constant, constant);
		assertEquals(constant, new PushConstant(List.of(one, two)));
		assertNotEquals(constant, null);
		assertNotEquals(constant, new PushConstant(List.of(one)));
	}

	@DisplayName("A push constant range...")
	@Nested
	class RangeTests {
		@Test
		void constructor() {
			assertEquals(0, one.offset());
			assertEquals(4, one.size());
			assertEquals(Set.of(VERTEX), one.stages());
		}

		@DisplayName("cannot be empty")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new Range(0, 0, Set.of(VERTEX)));
		}

		@DisplayName("must have at least one shader stage")
		@Test
		void stages() {
			assertThrows(IllegalArgumentException.class, () -> new Range(0, 4, Set.of()));
		}

		@DisplayName("must adhere to the alignment rules for the offset and size of the range")
		@Test
		void alignment() {
			assertThrows(IllegalArgumentException.class, () -> new Range(0, 3, Set.of(VERTEX)));
			assertThrows(IllegalArgumentException.class, () -> new Range(1, 4, Set.of(VERTEX)));
		}

		@Test
		void populate() {
			final var struct = new VkPushConstantRange();
			two.populate(struct);
			assertEquals(4, struct.offset);
			assertEquals(8, struct.size);
			assertEquals(EnumMask.of(FRAGMENT), struct.stageFlags);
		}
	}

	@DisplayName("A push constant update command...")
	@Nested
	class UpdateTests {
		private Command.CommandBuffer cmd;
		private PipelineLayout layout;
		private Command update;

		@BeforeEach
		void before() {
			layout = new PipelineLayout(new Handle(2), new MockDeviceContext(), constant);
			update = constant.update(one, layout);
			cmd = new MockCommandBuffer();
		}

		@Test
		void record() {
			final var lib = mock(VulkanLibrary.class);
			update.record(lib, cmd);
			verify(lib).vkCmdPushConstants(cmd, layout, EnumMask.of(VERTEX), 0, 4, constant.buffer());
		}

		@Test
		void range() {
			final Range other = new Range(0, 4, Set.of(ALL));
			assertThrows(IllegalArgumentException.class, () -> constant.update(other, layout));
		}

		@Test
		void whole() {
			assertThrows(IllegalArgumentException.class, () -> constant.update(layout));
		}

		@Test
		void layout() {
			final var other = new PipelineLayout(new Handle(3), new MockDeviceContext(), PushConstant.NONE);
			assertThrows(IllegalArgumentException.class, () -> constant.update(one, other));
		}
	}

	@Nested
	class Empty {
		@Test
		void length() {
			assertEquals(0, PushConstant.NONE.length());
		}

		@Test
		void buffer() {
			assertThrows(IllegalStateException.class, () -> PushConstant.NONE.buffer());
		}

		@Test
		void update() {
			final var layout = new PipelineLayout(new Handle(3), new MockDeviceContext(), PushConstant.NONE);
			assertThrows(IllegalArgumentException.class, () -> PushConstant.NONE.update(layout));
		}
	}
}
