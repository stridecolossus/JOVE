package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;

import com.sun.jna.Pointer;

public class AbstractTransientNativeObjectTest {
	private AbstractTransientNativeObject obj;
	private Pointer ptr;
	private boolean destroyed;

	@BeforeEach
	void before() {
		ptr = new Pointer(42);
		destroyed = false;
		obj = new AbstractTransientNativeObject(new Handle(ptr)) {
			@Override
			protected void release() {
				destroyed = true;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(new Handle(ptr), obj.handle());
		assertEquals(false, obj.isDestroyed());
		assertEquals(false, destroyed);
	}

	@Test
	void destroy() {
		obj.destroy();
		assertEquals(true, obj.isDestroyed());
		assertEquals(true, destroyed);
	}

	@Test
	void destroyAlreadyDestroyed() {
		obj.destroy();
		assertThrows(IllegalStateException.class, () -> obj.destroy());
	}

	@Test
	void restore() {
		obj.destroy();
		obj.restore();
		assertEquals(false, obj.isDestroyed());
	}

	@Test
	void restoreNotDestroyed() {
		assertThrows(IllegalStateException.class, () -> obj.restore());
	}
}
