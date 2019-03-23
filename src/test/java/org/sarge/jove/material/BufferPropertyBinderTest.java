package org.sarge.jove.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.material.Material.Property;

public class BufferPropertyBinderTest {
	private Property.Binder binder;
	private Shader.Parameter param;

	@BeforeEach
	public void before() {
		final Bufferable obj = new Bufferable() {
			@Override
			public int size() {
				return 2;
			}

			@Override
			public void buffer(FloatBuffer buffer) {
				buffer.put(1).put(2);
			}
		};
		binder = new BufferPropertyBinder(2, () -> obj);
		param = mock(Shader.Parameter.class);
	}

	@Test
	public void constructor() {
		assertEquals(2, binder.size());
	}

	@Test
	public void apply() {
		// Apply buffer to parameter
		final ArgumentCaptor<FloatBuffer> captor = ArgumentCaptor.forClass(FloatBuffer.class);
		binder.apply(param);
		verify(param).set(captor.capture());

		// Check generated buffer
		final FloatBuffer buffer = captor.getValue();
		assertEquals(2, buffer.capacity());
		assertEquals(0, buffer.position());

		// Check data
		buffer.limit(2);
		assertFloatEquals(1, buffer.get());
		assertFloatEquals(2, buffer.get());
	}

	@Test
	public void matrix() {
		binder = BufferPropertyBinder.matrix(() -> Matrix.IDENTITY);
		assertNotNull(binder);
		assertEquals(4 * 4, binder.size());
		binder.apply(param);
		verify(param).set(any(FloatBuffer.class));
	}

	@Test
	public void tuple() {
		binder = BufferPropertyBinder.tuple(() -> Point.ORIGIN);
		assertNotNull(binder);
		assertEquals(3, binder.size());
		binder.apply(param);
		verify(param).set(any(FloatBuffer.class));
	}

	@Test
	public void colour() {
		binder = BufferPropertyBinder.colour(() -> Colour.WHITE);
		assertNotNull(binder);
		assertEquals(4, binder.size());
		binder.apply(param);
		verify(param).set(any(FloatBuffer.class));
	}
}
