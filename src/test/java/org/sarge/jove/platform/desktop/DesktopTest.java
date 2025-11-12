package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;

class DesktopTest {
	static class MockDesktopLibrary implements DesktopLibrary {
		private final SegmentAllocator allocator = Arena.ofAuto();
		private int init = 1;
		private int error;
		private boolean terminated;

		@Override
		public void glfwInitHint(int hint, int value) {
			// TODO
		}

		@Override
		public int glfwInit() {
			return init;
		}

		@Override
		public int glfwGetError(Pointer description) {
			if(error == 0) {
				return 0;
			}
			else {
    			final MemorySegment pointer = allocator.allocateFrom("error");
    			description.set(new Handle(pointer));
    			return error;
			}
		}

		@Override
		public String glfwGetVersionString() {
			return "version";
		}

		@Override
		public boolean glfwVulkanSupported() {
			return true;
		}

		@Override
		public Handle glfwGetRequiredInstanceExtensions(IntegerReference count) {
			final MemorySegment extensions = allocator.allocate(ValueLayout.ADDRESS, 1);
			extensions.setAtIndex(ValueLayout.ADDRESS, 0L, allocator.allocateFrom("extension"));
			count.set(1);
			return new Handle(extensions);
		}

		@Override
		public void glfwTerminate() {
			assertEquals(false, terminated);
			terminated = true;
		}
	}

	private Desktop desktop;
	private MockDesktopLibrary library;

	@BeforeEach
	void before() {
		library = new MockDesktopLibrary();
		desktop = new Desktop(library);
		assertEquals(false, desktop.isDestroyed());
	}

	@Test
	void init() {
		library.init = 42;
		assertThrows(RuntimeException.class, () -> new Desktop(library));
	}

	@Test
	void error() {
		library.error = 42;
		assertEquals(Optional.of("[42] error"), desktop.error());
	}

	@Test
	void none() {
		assertEquals(Optional.empty(), desktop.error());
	}

	@Test
	void version() {
		assertEquals("version", desktop.version());
	}

	@Test
	void isVulkanSupported() {
		assertEquals(true, desktop.isVulkanSupported());
	}

	@Test
	void extensions() {
		assertEquals(List.of("extension"), desktop.extensions());
	}

	@Test
	void terminate() {
		desktop.destroy();
		assertEquals(true, library.terminated);
		assertEquals(true, desktop.isDestroyed());
	}
}
