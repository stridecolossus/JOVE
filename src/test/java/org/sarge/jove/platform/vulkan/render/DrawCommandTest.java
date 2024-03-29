package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;
import org.sarge.jove.platform.vulkan.core.*;

class DrawCommandTest {
	private Command.Buffer cmd;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		cmd = new MockCommandBuffer();
		lib = mock(VulkanLibrary.class);
	}

	@DisplayName("Create a simple draw command")
	@Test
	void draw() {
		final DrawCommand draw = DrawCommand.draw(2);
		draw.record(lib, cmd);
		verify(lib).vkCmdDraw(cmd, 2, 1, 0, 0);
	}

	@DisplayName("Create an indexed draw command")
	@Test
	void indexed() {
		final DrawCommand draw = DrawCommand.indexed(2);
		draw.record(lib, cmd);
		verify(lib).vkCmdDrawIndexed(cmd, 2, 1, 0, 0, 0);
	}

	@DisplayName("Create a draw command for a mesh")
	@Test
	void model() {
		final var builder = new IndexedMeshBuilder(Primitive.POINT, new CompoundLayout(Point.LAYOUT));
		builder.add(new Vertex(Point.ORIGIN));
		builder.add(0);
		builder.add(0);

		final DrawCommand draw = DrawCommand.of(builder.mesh());
		draw.record(lib, cmd);
		verify(lib).vkCmdDrawIndexed(cmd, 2, 1, 0, 0, 0);
	}

	@Nested
	class BuilderTest {
		private DrawCommand.Builder builder;

		@BeforeEach
		void before() {
			builder = new DrawCommand.Builder();
		}

		@DisplayName("Draw a number of vertices")
		@Test
		void simple() {
			builder.count(2).build().record(lib, cmd);
			verify(lib).vkCmdDraw(cmd, 2, 1, 0, 0);
		}

		@DisplayName("Draw instanced vertices")
		@Test
		void instanced() {
			builder
					.instances(1)
					.firstInstance(2)
					.build()
					.record(lib, cmd);

			verify(lib).vkCmdDraw(cmd, 0, 1, 0, 2);
		}

		@DisplayName("Draw indexed vertices")
		@Test
		void indexed() {
			builder.indexed().build().record(lib, cmd);
			verify(lib).vkCmdDrawIndexed(cmd, 0, 1, 0, 0, 0);
		}

		@DisplayName("Draw instanced and indexed vertices")
		@Test
		void instancedIndexed() {
			builder
					.count(1)
					.instances(2)
					.indexed(3)
					.firstVertex(4)
					.firstInstance(5)
					.build()
					.record(lib, cmd);

			verify(lib).vkCmdDrawIndexed(cmd, 1, 2, 3, 4, 5);
		}
	}

// TODO
//	@Nested
//	class IndirectBuilderTest {
//		private IndirectBuilder builder;
//		private VulkanBuffer buffer;
//
//		@BeforeEach
//		void before() {
//			builder = new IndirectBuilder();
//			buffer = new VulkanBuffer(dev, Set.of(VkBufferUsageFlag.INDIRECT_BUFFER), mock(DeviceMemory.class), 5);
//		}
//
//		private void init(int size) {
//			limit("maxDrawIndirectCount", size);
//		}
//
//		@DisplayName("Indirect draw")
//		@Test
//		void build() {
//			// Init device limit
//			init(3);
//
//			// Invoke indirect draw
//			builder
//					.offset(2)
//					.count(3)
//					.stride(4)
//					.build(buffer)
//					.record(lib, cmd);
//
//			// Check API
//			verify(lib).vkCmdDrawIndirect(cmd, buffer, 2, 3, 4);
//			verify(dev.limits()).require("multiDrawIndirect");
//		}
//
//		@DisplayName("Indirect indexed draw")
//		@Test
//		void indexed() {
//			init(1);
//			builder.indexed().build(buffer).record(lib, cmd);
//			verify(lib).vkCmdDrawIndexedIndirect(cmd, buffer, 0, 1, 0);
//		}
//
//		@DisplayName("Draw count cannot exceed the hardware limit")
//		@Test
//		void buildInvalidDrawCount() {
//			init(1);
//			builder.count(2);
//			assertThrows(IllegalArgumentException.class, () -> builder.build(buffer));
//		}
//	}
}
