package org.sarge.jove.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;

public class HandleTest {
	private Handle handle;
	private Pointer ptr;

	@BeforeEach
	public void before() {
		ptr = mock(Pointer.class);
		handle = new Handle(ptr) {
			// Empty implementation
		};
	}

	@Test
	public void constructor() {
		assertEquals(ptr, handle.handle());
		assertEquals(false, handle.isDestroyed());
	}

	@Test
	public void handleDestroyed() {
		handle.destroy();
		assertThrows(IllegalStateException.class, () -> handle.handle());
	}

	@Test
	public void destroy() {
		handle.destroy();
		assertEquals(true, handle.isDestroyed());
	}

	@Test
	public void destroyAlreadyDestroyed() {
		handle.destroy();
		assertThrows(IllegalStateException.class, () -> handle.destroy());
	}
}
