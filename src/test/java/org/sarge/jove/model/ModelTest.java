package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model.AbstractModel;
import org.sarge.jove.model.Model.Header;

class ModelTest {
	private Header header;
	private Layout layout;

	@BeforeEach
	void before() {
		layout = Layout.of(2);
		header = new Header(CompoundLayout.of(layout), Primitive.TRIANGLES, 3);
	}

	@Nested
	class HeaderTests {
		@Test
		void constructor() {
			assertEquals(CompoundLayout.of(layout), header.layout());
			assertEquals(Primitive.TRIANGLES, header.primitive());
			assertEquals(3, header.count());
		}

		@Test
		void invalidVertexCount() {
			assertThrows(IllegalArgumentException.class, () -> new Header(CompoundLayout.of(layout), Primitive.TRIANGLES, 2));
		}

		@Test
		void invalidPrimitiveNormals() {
			assertThrows(IllegalArgumentException.class, () -> new Header(CompoundLayout.of(Vector.NORMALS), Primitive.LINES, 2));
		}

		@Test
		void equals() {
			assertEquals(true, header.equals(header));
			assertEquals(true, header.equals(new Header(CompoundLayout.of(layout), Primitive.TRIANGLES, 3)));
			assertEquals(false, header.equals(null));
			assertEquals(false, header.equals(new Header(CompoundLayout.of(layout), Primitive.LINE_STRIP, 3)));
		}
	}

	@Nested
	class AbstractModelTests {
		@Test
		void constructor() {
			final Model model = new AbstractModel(header) {
				@Override
				public Bufferable vertices() {
					return null;
				}

				@Override
				public boolean isIndexed() {
					return false;
				}

				@Override
				public Optional<Bufferable> index() {
					return null;
				}
			};
			assertEquals(header, model.header());
		}
	}
}
