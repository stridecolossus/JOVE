package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Command;

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
		final Command draw = DrawCommand.draw(COUNT);
		assertNotNull(draw);
		draw.execute(lib, buffer);
		verify(lib).vkCmdDraw(buffer, COUNT, 1, 0, 0);
	}

	@Test
	void indexed() {
		final Command draw = DrawCommand.indexed(COUNT);
		assertNotNull(draw);
		draw.execute(lib, buffer);
		verify(lib).vkCmdDrawIndexed(buffer, COUNT, 1, 0, 0, 0);
	}

	@Test
	void of() {
		// Create a model
		final Model model = mock(Model.class);
		when(model.count()).thenReturn(COUNT);

		// Create draw command
		final Command draw = DrawCommand.of(model);
		assertNotNull(draw);
		draw.execute(lib, buffer);
		verify(lib).vkCmdDraw(buffer, COUNT, 1, 0, 0);

		// Create draw command for indexed model
		when(model.index()).thenReturn(Optional.of(ByteBuffer.allocate(0)));
		final Command indexed = DrawCommand.of(model);
		assertNotNull(indexed);
		indexed.execute(lib, buffer);
		verify(lib).vkCmdDrawIndexed(buffer, COUNT, 1, 0, 0, 0);
	}
}
