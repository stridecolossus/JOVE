package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.Mask;

class AbstractModelTest {
	private AbstractModel model;

	@BeforeEach
	void before() {
		model = new AbstractModel(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT)) {
			@Override
			public int count() {
				return 0;
			}

			@Override
			public Bufferable vertexBuffer() {
				return null;
			}

			@Override
			public Optional<Bufferable> indexBuffer() {
				return null;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
	}

	@Test
	void validate() {
		model.validate();
	}

	@Test
	void equals() {
		assertEquals(model, model);
		assertNotEquals(model, null);
		assertNotEquals(model, mock(Model.class));
	}

	@SuppressWarnings("static-method")
	@Test
	void isIntegerIndex() {
		final long max = Mask.unsignedMaximum(Short.SIZE);
		assertEquals(false, Model.isIntegerIndex(0));
		assertEquals(false, Model.isIntegerIndex(max - 1));
		assertEquals(true, Model.isIntegerIndex(max));
		assertEquals(true, Model.isIntegerIndex(Mask.unsignedMaximum(Integer.SIZE)));
	}
}
