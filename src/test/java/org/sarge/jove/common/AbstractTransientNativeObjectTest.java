package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import com.sun.jna.Pointer;

public class AbstractTransientNativeObjectTest {
	private AbstractTransientNativeObject obj;
	private Pointer ptr;
	private boolean destroyed;

	@BeforeEach
	void before() {
		ptr = new Pointer(42);
		destroyed = false;
		obj = new AbstractTransientNativeObject(ptr) {
			@Override
			protected void release() {
				destroyed = true;
			}
		};
	}

	@DisplayName("A transient object has a native handle")
	@Test
	void constructor() {
		assertEquals(new Handle(ptr), obj.handle());
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
