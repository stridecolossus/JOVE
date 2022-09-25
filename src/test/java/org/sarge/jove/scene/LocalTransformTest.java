package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

public class LocalTransformTest {
	private LocalTransform local;
	private Matrix x;

	@BeforeEach
	void before() {
		x = Matrix4.translation(Axis.X);
		local = new LocalTransform(x);
	}

	@DisplayName("The world matrix for a new local transform is initially undefined")
	@Test
	void constructor() {
		assertEquals(x, local.transform());
		assertEquals(true, local.isDirty());
		assertEquals(null, local.matrix());
		assertEquals(false, local.isMutable());
	}

	@DisplayName("The world matrix of a local transform can be initialised")
	@Test
	void update() {
		local.update(null);
		assertEquals(false, local.isDirty());
		assertEquals(x, local.matrix());
	}

	@DisplayName("The local transform can be composed with a parent transform")
	@Test
	void compose() {
		final Matrix y = Matrix4.translation(Axis.Y);
		final LocalTransform parent = new LocalTransform(y);
		parent.update(null);
		local.update(parent);
		assertEquals(false, local.isDirty());
		assertEquals(y.multiply(x), local.matrix());
	}
}
