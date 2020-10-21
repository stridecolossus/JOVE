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
	private Model model;
	private VulkanLibrary lib;
	private Handle handle;

	@BeforeEach
	void before() {
		lib = mock(VulkanLibrary.class);
		handle = new Handle(new Pointer(1));
		model = mock(Model.class);
		when(model.count()).thenReturn(42);
	}

	@Test
	void of() {
		final Command draw = DrawCommand.of(model);
		assertNotNull(draw);
		draw.execute(lib, handle);
		verify(lib).vkCmdDraw(handle, 42, 1, 0, 0);
	}

	@Test
	void indexed() {
		when(model.index()).thenReturn(Optional.of(ByteBuffer.allocate(1)));
		final Command draw = DrawCommand.of(model);
		assertNotNull(draw);
		draw.execute(lib, handle);
		verify(lib).vkCmdDrawIndexed(handle, 42, 1, 0, 0, 0);
	}
}
