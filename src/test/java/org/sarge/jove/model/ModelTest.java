package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.model.Model.AbstractModel;
import org.sarge.jove.model.Model.Header;

class ModelTest {
	private Header header;
	private Layout layout;

	@BeforeEach
	void before() {
		layout = Layout.of(2, Float.class);
		header = new Header(List.of(layout), Primitive.TRIANGLES, 3, true);
	}

	@Nested
	class HeaderTests {
		@Test
		void constructor() {
			assertEquals(List.of(layout), header.layout());
			assertEquals(Primitive.TRIANGLES, header.primitive());
			assertEquals(3, header.count());
			assertEquals(true, header.clockwise());
		}

		@Test
		void layout() {
			assertEquals(2 * Float.BYTES, header.stride());
			assertEquals(2 * Float.BYTES * 3, header.length());
		}

		@Test
		void invalidVertexCount() {
			assertThrows(IllegalArgumentException.class, () -> new Header(List.of(layout), Primitive.TRIANGLES, 2, true));
		}

		@Test
		void invalidPrimitiveNormals() {
// TODO
//			assertThrows(IllegalArgumentException.class, () -> new Header(Primitive.LINES, new Layout(Component.NORMAL), true));
		}

		@Test
		void equals() {
			assertEquals(true, header.equals(header));
			assertEquals(true, header.equals(new Header(List.of(layout), Primitive.TRIANGLES, 3, true)));
			assertEquals(false, header.equals(null));
			assertEquals(false, header.equals(new Header(List.of(layout), Primitive.LINE_STRIP, 3, true)));
		}
	}

	@Nested
	class AbstractModelTests {
		private Model model;

		@BeforeEach
		void before() {
			model = new AbstractModel(header) {
				@Override
				public Bufferable vertexBuffer() {
					return null;
				}

				@Override
				public boolean isIndexed() {
					return false;
				}

				@Override
				public Optional<Bufferable> indexBuffer() {
					return null;
				}
			};
		}

		@Test
		void constructor() {
			assertEquals(header, model.header());
		}
	}
}
