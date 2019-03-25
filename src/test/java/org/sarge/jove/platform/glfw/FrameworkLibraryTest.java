package org.sarge.jove.platform.glfw;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.glfw.FrameworkLibrary;

public class FrameworkLibraryTest {
	@Tag("GLFW")
	@Test
	public void create() {
		final FrameworkLibrary lib = FrameworkLibrary.create();
		assertNotNull(lib);
	}
}