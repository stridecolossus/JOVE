package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.Layout;

public class ModelTest {
	@Nested
	class HeaderTests {
		private Header header;
		private Layout layout;

		@BeforeEach
		void before() {
			layout = new Layout(Component.POSITION);
			header = new Header(Primitive.TRIANGLES, layout, true);
		}

		@Test
		void constructor() {
			assertEquals(Primitive.TRIANGLES, header.primitive());
			assertEquals(layout, header.layout());
			assertEquals(true, header.clockwise());
		}

		@Test
		void invalidPrimitiveNormals() {
			assertThrows(IllegalArgumentException.class, () -> new Header(Primitive.LINES, new Layout(Component.NORMAL), true));
		}

		@Test
		void equals() {
			assertEquals(true, header.equals(header));
			assertEquals(true, header.equals(new Header(Primitive.TRIANGLES, layout, true)));
			assertEquals(false, header.equals(null));
			assertEquals(false, header.equals(new Header(Primitive.LINE_STRIP, layout, true)));
		}
	}
}
