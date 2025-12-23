package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class AbstractTransientObjectTest {
	private AbstractTransientObject object;
	private boolean destroyed;

	@BeforeEach
	void before() {
		destroyed = false;
		object = new AbstractTransientObject() {
			@Override
			protected void release() {
				destroyed = true;
			}
		};
	}

	@DisplayName("A new transient object is not destroyed")
	@Test
	void isDestroyed() {
		assertEquals(false, object.isDestroyed());
		assertEquals(false, destroyed);
	}

	@DisplayName("A transient object can be destroyed")
	@Test
	void destroy() {
		object.destroy();
		assertEquals(true, object.isDestroyed());
		assertEquals(true, destroyed);
	}

	@DisplayName("A transient object cannot be destroyed more than once")
	@Test
	void already() {
		object.destroy();
		assertThrows(IllegalStateException.class, () -> object.destroy());
	}
}
