package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;

class AbstractModelTest {
	private AbstractModel model;

	@BeforeEach
	void before() {
		model = new AbstractModel(Primitive.TRIANGLE_STRIP, CompoundLayout.of(Point.LAYOUT)) {
			@Override
			public int count() {
				return 0;
			}

			@Override
			public Bufferable vertices() {
				return null;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(CompoundLayout.of(Point.LAYOUT), model.layout());
		assertEquals(false, model.isIndexed());
		assertEquals(Optional.empty(), model.index());
	}

	@DisplayName("The draw count must logically match the drawing primitive")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> model.validate(2));
	}

	@DisplayName("The index buffer for a model can be stored as integer or short values")
	@Test
	void isIntegerIndex() {
		final int max = 65535;
		assertEquals(false, Model.isIntegerIndex(0));
		assertEquals(false, Model.isIntegerIndex(max - 1));
		assertEquals(true, Model.isIntegerIndex(max));
	}
}
