package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class AbstractNativeObjectTest {
	private AbstractNativeObject object;

	@BeforeEach
	void before() {
		object = new AbstractNativeObject(new Handle(42)) {
			// Empty
		};
	}

	@Test
	void constructor() {
		assertEquals(new Handle(42), object.handle());
		assertEquals(false, object.isDestroyed());
	}

	@Test
	void equals() {
		assertEquals(object, object);
		assertNotEquals(object, null);
	}
}
