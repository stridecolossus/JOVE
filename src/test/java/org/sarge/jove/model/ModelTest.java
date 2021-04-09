package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model.Header;

public class ModelTest {
	@Nested
	class HeaderTests {
		private Header header;

		@BeforeEach
		void before() {
			header = new Header(Primitive.TRIANGLES, true, 3);
		}

		@Test
		void constructor() {
			assertEquals(Primitive.TRIANGLES, header.primitive());
			assertEquals(true, header.clockwise());
			assertEquals(3, header.count());
		}

		@Test
		void invalidVertexCount() {
			assertThrows(IllegalArgumentException.class, () -> new Header(Primitive.TRIANGLES, true, 2));
		}

		@Test
		void equals() {
			assertEquals(true, header.equals(header));
			assertEquals(true, header.equals(new Header(Primitive.TRIANGLES, true, 3)));
			assertEquals(false, header.equals(null));
			assertEquals(false, header.equals(new Header(Primitive.TRIANGLES, true, 0)));
		}
	}
}
