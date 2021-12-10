package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.DrawCommand.Builder;

public class DrawCommandTest {
	private static final int COUNT = 42;

	private VulkanLibrary lib;
	private Command.Buffer buffer;

	@BeforeEach
	void before() {
		lib = mock(VulkanLibrary.class);
		buffer = mock(Command.Buffer.class);
	}

	@Test
	void draw() {
		final DrawCommand draw = DrawCommand.draw(COUNT);
		assertNotNull(draw);
		draw.execute(lib, buffer);
		verify(lib).vkCmdDraw(buffer, COUNT, 1, 0, 0);
	}

	@Test
	void indexed() {
		final DrawCommand draw = DrawCommand.indexed(COUNT);
		assertNotNull(draw);
		draw.execute(lib, buffer);
		verify(lib).vkCmdDrawIndexed(buffer, COUNT, 1, 0, 0, 0);
	}

	@Test
	void model() {
		// Create an indexed model
		final Model.Header header = new Model.Header(List.of(), Primitive.TRIANGLES, COUNT);
		final Model model = mock(Model.class);
		when(model.isIndexed()).thenReturn(true);
		when(model.header()).thenReturn(header);

		// Check indexed draw command
		final DrawCommand draw = DrawCommand.of(model);
		draw.execute(lib, buffer);
		verify(lib).vkCmdDrawIndexed(buffer, COUNT, 1, 0, 0, 0);
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		private void execute() {
			final DrawCommand cmd = builder.build();
			assertNotNull(cmd);
			cmd.execute(lib, buffer);
		}

		@Test
		void simple() {
			builder.count(COUNT);
			builder.firstVertex(2);
			execute();
			verify(lib).vkCmdDraw(buffer, COUNT, 1, 2, 0);
		}

		@Test
		void instanced() {
			builder.count(COUNT);
			builder.instances(2);
			builder.firstVertex(3);
			builder.firstInstance(4);
			execute();
			verify(lib).vkCmdDraw(buffer, COUNT, 2, 3, 4);
			execute();
		}

		@Test
		void indexed() {
			builder.count(COUNT);
			builder.indexed();
			builder.firstIndex(2);
			builder.firstVertex(3);
			execute();
			verify(lib).vkCmdDrawIndexed(buffer, COUNT, 1, 2, 3, 0);
		}

		@Test
		void instancedIndexed() {
			builder.count(COUNT);
			builder.indexed();
			builder.instances(2);
			builder.firstIndex(3);
			builder.firstVertex(4);
			builder.firstInstance(5);
			execute();
			verify(lib).vkCmdDrawIndexed(buffer, COUNT, 2, 3, 4, 5);
			execute();
		}
	}
}
