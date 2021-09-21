package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.DrawCommand.Builder;

import com.sun.jna.Pointer;

public class DrawCommandTest {
	private static final int COUNT = 42;

	private VulkanLibrary lib;
	private Handle buffer;

	@BeforeEach
	void before() {
		lib = mock(VulkanLibrary.class);
		buffer = new Handle(new Pointer(1));
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
		void indexed() {
			builder.indexed(2);
			builder.count(COUNT);
			builder.firstVertex(3);
			execute();
			verify(lib).vkCmdDrawIndexed(buffer, COUNT, 1, 2, 3, 0);
		}

		@Test
		void instanced() {
			builder.instanced(2, 5);
			builder.indexed(3);
			builder.count(COUNT);
			builder.firstVertex(4);
			execute();
			verify(lib).vkCmdDrawIndexed(buffer, COUNT, 2, 3, 4, 5);
			execute();
		}
	}
}
