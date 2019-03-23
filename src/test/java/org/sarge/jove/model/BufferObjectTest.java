package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.BufferObject.Mode;

public class BufferObjectTest {
	private BufferObject buffer;

	@BeforeEach
	public void before() {
		buffer = new BufferObject(Mode.STATIC) {
			@Override
			public int size() {
				return 1;
			}

			@Override
			public int length() {
				return 1;
			}

			@Override
			public void push() {
				// Mock implementation
			}
		};
	}

	@Test
	public void constructor() {
		assertEquals(Mode.STATIC, buffer.mode());
	}

	@Test
	public void checkMutable() {
		assertThrows(IllegalStateException.class, () -> buffer.checkMutable());
	}
}
