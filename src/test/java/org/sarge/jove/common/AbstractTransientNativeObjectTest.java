package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class AbstractTransientNativeObjectTest {
	private AbstractTransientNativeObject obj;
	private Handle handle;
	private boolean destroyed;

	@BeforeEach
	void before() {
		handle = new Handle(1);
		destroyed = false;
		obj = new AbstractTransientNativeObject(handle) {
			@Override
			protected void release() {
				destroyed = true;
			}
		};
	}

	@DisplayName("A transient object has a native handle")
	@Test
	void constructor() {
		assertEquals(handle, obj.handle());
	}

	@DisplayName("A new transient object is not destroyed")
	@Test
	void isDestroyed() {
		assertEquals(false, obj.isDestroyed());
		assertEquals(false, destroyed);
	}

	@DisplayName("A transient object can be destroyed")
	@Test
	void destroy() {
		obj.destroy();
		assertEquals(true, obj.isDestroyed());
		assertEquals(true, destroyed);
	}

	@DisplayName("A transient object cannot be destroyed more than once")
	@Test
	void destroyAlreadyDestroyed() {
		obj.destroy();
		assertThrows(IllegalStateException.class, () -> obj.destroy());
	}
}
