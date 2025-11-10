package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PrimitiveTest {
	@DisplayName("The number of polygon vertices is...")
	@Nested
	class PolygonSizeTests {
		@DisplayName("one for points or patches")
		@ParameterizedTest
		@EnumSource(value=Primitive.class, names={"POINT", "PATCH"})
		void points(Primitive primitive) {
			assertEquals(1, primitive.size());
		}

		@DisplayName("two for lines")
		@ParameterizedTest
		@EnumSource(value=Primitive.class, names={"LINE.*"}, mode=MATCH_ALL)
		void lines(Primitive primitive) {
			assertEquals(2, primitive.size());
		}

		@DisplayName("three for triangular primitives")
		@ParameterizedTest
		@EnumSource(value=Primitive.class, names={"TRIANGLE.*"}, mode=MATCH_ALL)
		void triangles(Primitive primitive) {
			assertEquals(3, primitive.size());
		}
	}

	@DisplayName("Strip-based primitives can be distingished")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={".+STRIP", "TRIANGLE_FAN"}, mode=MATCH_ANY)
	void isStrip(Primitive primitive) {
		assertEquals(true, primitive.isStrip());
	}

	@DisplayName("Non strip-based primitives can be distingished")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={".+STRIP", "TRIANGLE_FAN"}, mode=MATCH_NONE)
	void isNotStrip(Primitive primitive) {
		assertEquals(false, primitive.isStrip());
	}

	@DisplayName("Triangular primitives are polygons")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE.*"}, mode=MATCH_ALL)
	void isTriangle(Primitive primitive) {
		assertEquals(true, primitive.isTriangle());
	}

	@DisplayName("Non-triangular primitives are not polygons")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE.*"}, mode=MATCH_NONE)
	void isNotTriangle(Primitive primitive) {
		assertEquals(false, primitive.isTriangle());
	}

	@DisplayName("All primitives should be valid for zero vertices or a multiple of the primitive size")
	@ParameterizedTest
	@EnumSource(Primitive.class)
	void isValidVertexCount(Primitive primitive) {
		assertEquals(true, primitive.isValidVertexCount(0));
		assertEquals(true, primitive.isValidVertexCount(primitive.size()));
		assertEquals(true, primitive.isValidVertexCount(2 * primitive.size()));
	}

	@DisplayName("Non strip-based primitives require exact multiples of the primitive size")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE", "LINE"})
	void invalidVertexCount(Primitive primitive) {
		assertEquals(false, primitive.isValidVertexCount(1));
		assertEquals(false, primitive.isValidVertexCount(primitive.size() + 1));
	}

	@Nested
	class PolygonTests {
		@ParameterizedTest
		@EnumSource(value=Primitive.class, names={"POINT", "PATCH"})
		void points(Primitive primitive) {
			assertEquals(0, primitive.polygons(0));
			assertEquals(1, primitive.polygons(1));
			assertEquals(2, primitive.polygons(2));
		}

		@Test
		void lines() {
			assertEquals(0, Primitive.LINE.polygons(0));
			assertEquals(0, Primitive.LINE.polygons(1));
			assertEquals(1, Primitive.LINE.polygons(2));
			assertEquals(1, Primitive.LINE.polygons(3));
		}

		@Test
		void strip() {
			assertEquals(0, Primitive.LINE_STRIP.polygons(1));
			assertEquals(1, Primitive.LINE_STRIP.polygons(2));
			assertEquals(2, Primitive.LINE_STRIP.polygons(3));
			assertEquals(3, Primitive.LINE_STRIP.polygons(4));
		}

		@Test
		void triangles() {
			assertEquals(0, Primitive.TRIANGLE.polygons(0));
			assertEquals(0, Primitive.TRIANGLE.polygons(1));
			assertEquals(0, Primitive.TRIANGLE.polygons(2));
			assertEquals(1, Primitive.TRIANGLE.polygons(3));
			assertEquals(1, Primitive.TRIANGLE.polygons(4));
			assertEquals(1, Primitive.TRIANGLE.polygons(5));
			assertEquals(2, Primitive.TRIANGLE.polygons(6));
		}

		@ParameterizedTest
		@EnumSource(value=Primitive.class, names={"TRIANGLE_STRIP", "TRIANGLE_FAN"})
		void strip(Primitive primitive) {
			assertEquals(0, primitive.polygons(2));
			assertEquals(1, primitive.polygons(3));
			assertEquals(2, primitive.polygons(4));
			assertEquals(3, primitive.polygons(5));
		}
	}
}
