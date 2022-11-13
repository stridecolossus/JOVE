package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

// TODO - the argument sources are really ugly, can we create a custom @ArgumentsSource that enumerates strip, triangles, etc and has a simple inversion?
class PrimitiveTest {
	@DisplayName("Primitives specify the number of vertices")
	@ParameterizedTest
	@EnumSource(Primitive.class)
	void size(Primitive primitive) {
		final int expected = switch(primitive) {
			case POINTS, PATCH -> 1;
			case LINES, LINE_STRIP -> 2;
			default -> 3;
		};
		assertEquals(expected, primitive.size());
	}
	// TODO - this sucks too

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

	@DisplayName("Triangle-based primitives should support normals")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE.+"}, mode=MATCH_ALL)
	void isNormalSupport(Primitive primitive) {
		assertEquals(true, primitive.isNormalSupported());
	}

	@DisplayName("Non triangle-based primitives should not support normals")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE.+"}, mode=MATCH_NONE)
	void isNotNormalSupport(Primitive primitive) {
		assertEquals(false, primitive.isNormalSupported());
	}

	@DisplayName("Triangular primitives are polygons")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE.+"}, mode=MATCH_ALL)
	void isTriangle(Primitive primitive) {
		assertEquals(true, primitive.isTriangle());
	}

	@DisplayName("Non-triangular primitives are not polygons")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE.+"}, mode=MATCH_NONE)
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
	@EnumSource(value=Primitive.class, names={"TRIANGLES", "LINES"})
	void invalidVertexCount(Primitive primitive) {
		assertEquals(false, primitive.isValidVertexCount(1));
		assertEquals(false, primitive.isValidVertexCount(primitive.size() + 1));
	}

	// TODO
	@Test
	void faces() {
		assertEquals(0, Primitive.TRIANGLES.faces(0));
		assertEquals(0, Primitive.TRIANGLES.faces(1));
		assertEquals(0, Primitive.TRIANGLES.faces(2));
		assertEquals(1, Primitive.TRIANGLES.faces(3));
		assertEquals(1, Primitive.TRIANGLES.faces(3));

		assertEquals(0, Primitive.LINES.faces(0));
		assertEquals(0, Primitive.LINES.faces(1));
		assertEquals(1, Primitive.LINES.faces(2));
		assertEquals(1, Primitive.LINES.faces(3));

		assertEquals(0, Primitive.POINTS.faces(0));
		assertEquals(1, Primitive.POINTS.faces(1));
		assertEquals(2, Primitive.POINTS.faces(2));
	}
}
