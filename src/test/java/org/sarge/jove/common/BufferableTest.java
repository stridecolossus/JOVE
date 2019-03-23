package org.sarge.jove.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.BufferFactory;

public class BufferableTest {
	private Colour col;

	@BeforeEach
	public void before() {
		col = new Colour(0.1f, 0.2f, 0.3f, 0.4f);
	}

	@Test
	public void create() {
		final FloatBuffer buffer = Bufferable.create(col, col);
		assertNotNull(buffer);
		assertEquals(2 * 4, buffer.capacity());
		assertEquals(0, buffer.position());
		for(int n = 0; n < 2; ++n) {
			assertFloatEquals(0.1f, buffer.get());
			assertFloatEquals(0.2f, buffer.get());
			assertFloatEquals(0.3f, buffer.get());
			assertFloatEquals(0.4f, buffer.get());
		}
	}

	@Test
	public void createNullElement() {
		assertThrows(NullPointerException.class, () -> Bufferable.create((Bufferable) null));
	}

	@Test
	public void populate() {
		final FloatBuffer buffer = BufferFactory.floatBuffer(5);
		Bufferable.populate(buffer, 1, col);
		buffer.flip();
		assertFloatEquals(0.0f, buffer.get());
		assertFloatEquals(0.1f, buffer.get());
		assertFloatEquals(0.2f, buffer.get());
		assertFloatEquals(0.3f, buffer.get());
		assertFloatEquals(0.4f, buffer.get());
	}
}
