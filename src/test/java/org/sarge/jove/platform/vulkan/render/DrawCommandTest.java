package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.render.DrawCommand.Builder;
import org.sarge.jove.platform.vulkan.render.DrawCommand.IndirectBuilder;
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
		draw.execute(lib, cmd);
		verify(lib).vkCmdDraw(cmd, 2, 1, 0, 0);
	}

	@DisplayName("Create an indexed draw command")
	@Test
	void indexed() {
		final DrawCommand draw = DrawCommand.indexed(2);
		assertNotNull(draw);
		draw.execute(lib, cmd);
		verify(lib).vkCmdDrawIndexed(cmd, 2, 1, 0, 0, 0);
	}

	@DisplayName("Create a draw command for a model")
	@Test
	void model() {
		// Create an indexed model
		final Model model = mock(Model.class);
		when(model.isIndexed()).thenReturn(true);
		when(model.count()).thenReturn(2);
		when(model.primitive()).thenReturn(Primitive.TRIANGLES);

		// Check indexed draw command
		final DrawCommand draw = DrawCommand.of(model);
		draw.execute(lib, cmd);
		verify(lib).vkCmdDrawIndexed(cmd, 2, 1, 0, 0, 0);
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
			builder.count(2).build().execute(lib, cmd);
			verify(lib).vkCmdDraw(cmd, 2, 1, 0, 0);
		}

		@DisplayName("Draw instanced vertices")
		@Test
		void instanced() {
			builder
					.instances(1)
					.firstInstance(2)
					.build()
					.execute(lib, cmd);

			verify(lib).vkCmdDraw(cmd, 0, 1, 0, 2);
		}

		@DisplayName("Draw indexed vertices")
		@Test
		void indexed() {
			builder.indexed().build().execute(lib, cmd);
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
					.execute(lib, cmd);

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
			buffer = mock(VulkanBuffer.class);
			when(buffer.device()).thenReturn(dev);
		}

		@DisplayName("Indirect draw")
		@Test
		void build() {
			// Init device limit
			property(IndirectBuilder.MULTIDRAW, 3f, true);

			// Invoke indirect draw
			builder
					.offset(2)
					.count(3)
					.stride(4)
					.build(buffer)
					.execute(lib, cmd);

			// Check API
			verify(lib).vkCmdDrawIndirect(cmd, buffer, 2, 3, 4);
			verify(buffer).require(VkBufferUsageFlag.INDIRECT_BUFFER);
			verify(buffer).validate(2);
		}

		@DisplayName("Indirect indexed draw")
		@Test
		void indexed() {
			property(IndirectBuilder.MULTIDRAW, 1f, true);
			builder.indexed().build(buffer).execute(lib, cmd);
			verify(lib).vkCmdDrawIndexedIndirect(cmd, buffer, 0, 1, 0);
			verify(buffer).require(VkBufferUsageFlag.INDIRECT_BUFFER);
		}

		@DisplayName("Draw count cannot exceed the hardware limit")
		@Test
		void buildInvalidDrawCount() {
			property(IndirectBuilder.MULTIDRAW, 1f, true);
			builder.count(2);
			assertThrows(IllegalArgumentException.class, () -> builder.build(buffer));
		}

		@DisplayName("Draw count other than zero or one must be a supported device feature")
		@Test
		void buildDrawCountNotSupported() {
			property(IndirectBuilder.MULTIDRAW, 2f, false);
			builder.count(2);
			assertThrows(IllegalStateException.class, () -> builder.build(buffer));
		}
	}
}
