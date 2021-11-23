package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.MathsUtil.HALF;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MutableRotationTest {
	private MutableRotation rot;
	private Function<Rotation, Transform> mapper;

	@BeforeEach
	void before() {
		mapper = mock(Function.class);
		rot = new MutableRotation(Vector.Y, mapper);
	}

	@Test
	void constructor() {
		assertEquals(Vector.Y, rot.axis());
		assertEquals(0, rot.angle());
		assertEquals(true, rot.isDirty());
	}

	@Test
	void matrix() {
		when(mapper.apply(rot)).thenReturn(Matrix.IDENTITY);
		assertEquals(Matrix.IDENTITY, rot.matrix());
		assertEquals(false, rot.isDirty());
	}

	@Test
	void angle() {
		rot.angle(HALF);
		assertEquals(HALF, rot.angle());
	}

	@Test
	void equals() {
		assertEquals(true, rot.equals(rot));
		assertEquals(true, rot.equals(new MutableRotation(Vector.Y, mapper)));
		assertEquals(false, rot.equals(null));
		assertEquals(false, rot.equals(new MutableRotation(Vector.Z, mapper)));
	}
}
