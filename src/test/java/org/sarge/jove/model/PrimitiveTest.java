package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

	@DisplayName("Strip-based primitives can be distingished")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE_STRIP", "TRIANGLE_FAN", "LINE_STRIP"})
	void isStrip(Primitive primitive) {
		assertEquals(true, primitive.isStrip());
	}

	@DisplayName("Non strip-based primitives can be distingished")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLE_STRIP", "TRIANGLE_FAN", "LINE_STRIP"}, mode=EXCLUDE)
	void isNotStrip(Primitive primitive) {
		assertEquals(false, primitive.isStrip());
	}

	@DisplayName("Triangle-based primitives should support normals")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLES", "TRIANGLE_STRIP", "TRIANGLE_FAN"})
	void isNormalSupport(Primitive primitive) {
		assertEquals(true, primitive.isNormalSupported());
	}

	@DisplayName("Non triangle-based primitives should not support normals")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLES", "TRIANGLE_STRIP", "TRIANGLE_FAN"}, mode=EXCLUDE)
	void isNotNormalSupport(Primitive primitive) {
		assertEquals(false, primitive.isNormalSupported());
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

	@DisplayName("Triangle-based primitives should provide an index factory")
	@Test
	void index() {
		assertEquals(Optional.of(Triangle.INDEX_TRIANGLES), Primitive.TRIANGLES.index());
		assertEquals(Optional.of(Triangle.INDEX_STRIP), Primitive.TRIANGLE_STRIP.index());
	}

	@DisplayName("Other primitives should NOT provide an index factory")
	@ParameterizedTest
	@EnumSource(value=Primitive.class, names={"TRIANGLES", "TRIANGLE_STRIP"}, mode=EXCLUDE)
	void unindexed(Primitive primitive) {
		assertEquals(Optional.empty(), primitive.index());
	}
}
