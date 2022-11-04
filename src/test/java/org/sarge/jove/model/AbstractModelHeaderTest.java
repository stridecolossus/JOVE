package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

class AbstractModelHeaderTest {
	private AbstractModelHeader header;

	@BeforeEach
	void before() {
		header = new AbstractModelHeader(Primitive.TRIANGLES) {
			@Override
			public Layout layout() {
				return new Layout(Point.LAYOUT);
			}

			@Override
			public boolean isIndexed() {
				return false;
			}

			@Override
			public int count() {
				return 0;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLES, header.primitive());
	}

	@Test
	void equals() {
		assertEquals(header, header);
		assertNotEquals(header, null);
		assertNotEquals(header, mock(Header.class));
	}
}
