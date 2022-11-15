package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Mesh;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.render.DrawCommand.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

class DrawCommandTest extends AbstractVulkanTest {
	private Command.Buffer cmd;

	@BeforeEach
	void before() {
		cmd = mock(Command.Buffer.class);
	}

	@DisplayName("Create a simple draw command")
	@Test
	void draw() {
		final DrawCommand draw = DrawCommand.draw(2);
		assertNotNull(draw);
		draw.record(lib, cmd);
		verify(lib).vkCmdDraw(cmd, 2, 1, 0, 0);
	}

	@DisplayName("Create an indexed draw command")
	@Test
	void indexed() {
		final DrawCommand draw = DrawCommand.indexed(2);
		assertNotNull(draw);
		draw.record(lib, cmd);
		verify(lib).vkCmdDrawIndexed(cmd, 2, 1, 0, 0, 0);
	}

	@DisplayName("Create a draw command for a mesh")
	@Test
	void model() {
		final var mesh = mock(Mesh.class);
		when(mesh.count()).thenReturn(3);
		when(mesh.isIndexed()).thenReturn(true);

		final DrawCommand draw = DrawCommand.of(mesh);
		draw.record(lib, cmd);
		verify(lib).vkCmdDrawIndexed(cmd, 3, 1, 0, 0, 0);
	}

	@Nested
	class BuilderTest {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
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

	@Nested
	class IndirectBuilderTest {
		private IndirectBuilder builder;
		private VulkanBuffer buffer;

		@BeforeEach
		void before() {
			builder = new IndirectBuilder();
			buffer = VulkanBufferTest.create(dev, Set.of(VkBufferUsageFlag.INDIRECT_BUFFER), mock(DeviceMemory.class), 5);
		}

		private void init(int size) {
			limit("maxDrawIndirectCount", size);
		}

		@DisplayName("Indirect draw")
		@Test
		void build() {
			// Init device limit
			init(3);

			// Invoke indirect draw
			builder
					.offset(2)
					.count(3)
					.stride(4)
					.build(buffer)
					.record(lib, cmd);

			// Check API
			verify(lib).vkCmdDrawIndirect(cmd, buffer, 2, 3, 4);
			verify(dev.limits()).require("multiDrawIndirect");
		}

		@DisplayName("Indirect indexed draw")
		@Test
		void indexed() {
			init(1);
			builder.indexed().build(buffer).record(lib, cmd);
			verify(lib).vkCmdDrawIndexedIndirect(cmd, buffer, 0, 1, 0);
		}

		@DisplayName("Draw count cannot exceed the hardware limit")
		@Test
		void buildInvalidDrawCount() {
			init(1);
			builder.count(2);
			assertThrows(IllegalArgumentException.class, () -> builder.build(buffer));
		}
	}
}
