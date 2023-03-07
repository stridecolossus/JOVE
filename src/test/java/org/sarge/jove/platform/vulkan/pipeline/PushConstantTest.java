package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.*;
import org.sarge.jove.util.BitMask;

public class PushConstantTest {
	private static final Set<VkShaderStage> VERTEX = Set.of(VkShaderStage.VERTEX);

	private PushConstant constant;
	private Range one, two;

	@BeforeEach
	void before() {
		one = new Range(0, 4, VERTEX);
		two = new Range(4, 8, Set.of(VkShaderStage.FRAGMENT));
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
		final Range dup = new Range(4, 8, VERTEX);
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, dup)));
		assertThrows(IllegalArgumentException.class, () -> new PushConstant(List.of(one, one)));
	}

	@DisplayName("A push constant must have a range covering every portion of the buffer")
	@Test
	void coverage() {
		final Range three = new Range(8, 4, Set.of(VkShaderStage.FRAGMENT));
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
			assertEquals(VERTEX, one.stages());
		}

		@DisplayName("cannot be empty")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new Range(0, VERTEX));
		}

		@DisplayName("must have at least one shader stage")
		@Test
		void stages() {
			assertThrows(IllegalArgumentException.class, () -> new Range(4, Set.of()));
		}

		@DisplayName("must adhere to the alignment rules for the offset and size of the range")
		@Test
		void alignment() {
			assertThrows(IllegalArgumentException.class, () -> new Range(0, 3, VERTEX));
			assertThrows(IllegalArgumentException.class, () -> new Range(1, 4, VERTEX));
		}

		@Test
		void populate() {
			final var struct = new VkPushConstantRange();
			two.populate(struct);
			assertEquals(4, struct.offset);
			assertEquals(8, struct.size);
			assertEquals(BitMask.reduce(VkShaderStage.FRAGMENT), struct.stageFlags);
		}
	}

	@DisplayName("A push constant update command...")
	@Nested
	class UpdateTests {
		private Command.Buffer cmd;
		private PipelineLayout layout;
//		private Range range;

		@BeforeEach
		void before() {
			cmd = new MockCommandBuffer();
//			range = new Range(0, 4, Set.of(VkShaderStage.FRAGMENT));
			layout = new PipelineLayout(new Handle(2), new MockDeviceContext(), constant);
		}

//		@DisplayName("can be created for a given push constant range")
//		@Test
//		void update() {
//			final UpdateCommand update = constant.update(two, layout);
//			assertNotNull(update);
//		}
//
//		@DisplayName("can be created from a push constant with a single range")
//		@Test
//		void single() {
//			final PushConstant single = new PushConstant(List.of(one));
//			when(layout.push()).thenReturn(single);
//			single.update(layout);
//		}

		@DisplayName("must specify the range for a constant with multiple ranges")
		@Test
		void multiple() {
			assertThrows(IllegalStateException.class, () -> constant.update(layout));
		}

		@DisplayName("cannot be created from a push constant with a different pipeline layout")
		@Test
		void invalid() {
//			final PushConstant other = new PushConstant(List.of(range));
//			when(layout.push()).thenReturn(other);
//			assertThrows(IllegalStateException.class, () -> constant.update(layout));
		}

		@DisplayName("cannot be created for a push constant range that is not a member of the layout")
		@Test
		void member() {
			final var other = new PipelineLayout(new Handle(2), new MockDeviceContext(), new PushConstant(List.of()));
			assertThrows(IllegalStateException.class, () -> constant.update(one, other));
		}

		@DisplayName("cannot be created for an empty push constant")
		@Test
		void empty() {
			layout = new PipelineLayout(new Handle(2), new MockDeviceContext(), new PushConstant(List.of()));
			assertThrows(IllegalStateException.class, () -> constant.update(layout));
		}

		@DisplayName("can be applied to a given push constant range")
		@Test
		void apply() {
			final var lib = layout.device().library();
			final UpdateCommand update = constant.update(two, layout);
			update.record(lib, cmd);
			verify(lib).vkCmdPushConstants(cmd, layout, BitMask.reduce(VkShaderStage.FRAGMENT), 4, 8, constant.buffer());
		}
	}
}
