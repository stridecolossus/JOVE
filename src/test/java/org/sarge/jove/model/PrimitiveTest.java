package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class PrimitiveTest {
	@Test
	public void triangle() {
		assertEquals(3, Primitive.TRIANGLES.size());
		assertEquals(false, Primitive.TRIANGLES.isStrip());
		assertEquals(true, Primitive.TRIANGLES.isNormalSupported());
		assertEquals(true, Primitive.TRIANGLES.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLES.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLES.isValidVertexCount(3));
		assertEquals(false, Primitive.TRIANGLES.isValidVertexCount(4));
		assertEquals(true, Primitive.TRIANGLES.isValidVertexCount(6));
		assertNotNull(Primitive.TRIANGLES.index());
	}

	@Test
	public void triangleStrip() {
		assertEquals(3, Primitive.TRIANGLE_STRIP.size());
		assertEquals(true, Primitive.TRIANGLE_STRIP.isStrip());
		assertEquals(true, Primitive.TRIANGLE_STRIP.isNormalSupported());
		assertEquals(true, Primitive.TRIANGLE_STRIP.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLE_STRIP.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLE_STRIP.isValidVertexCount(3));
		assertEquals(true, Primitive.TRIANGLE_STRIP.isValidVertexCount(4));
		assertNotNull(Primitive.TRIANGLE_STRIP.index());
	}

	@Test
	public void triangleFan() {
		assertEquals(3, Primitive.TRIANGLE_FAN.size());
		assertEquals(true, Primitive.TRIANGLE_FAN.isStrip());
		assertEquals(true, Primitive.TRIANGLE_FAN.isNormalSupported());
		assertEquals(true, Primitive.TRIANGLE_FAN.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLE_FAN.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLE_FAN.isValidVertexCount(3));
		assertEquals(true, Primitive.TRIANGLE_FAN.isValidVertexCount(4));
//		assertNotNull(Primitive.TRIANGLES.index());
		// TODO
	}

	@Test
	public void point() {
		assertEquals(1, Primitive.POINTS.size());
		assertEquals(false, Primitive.POINTS.isStrip());
		assertEquals(false, Primitive.POINTS.isNormalSupported());
		assertEquals(true, Primitive.POINTS.isValidVertexCount(0));
		assertEquals(true, Primitive.POINTS.isValidVertexCount(1));
		assertEquals(true, Primitive.POINTS.isValidVertexCount(2));
		assertNull(Primitive.POINTS.index());
	}

	@Test
	public void line() {
		assertEquals(2, Primitive.LINES.size());
		assertEquals(false, Primitive.LINES.isStrip());
		assertEquals(false, Primitive.LINES.isNormalSupported());
		assertEquals(true, Primitive.LINES.isValidVertexCount(0));
		assertEquals(false, Primitive.LINES.isValidVertexCount(1));
		assertEquals(true, Primitive.LINES.isValidVertexCount(0));
		assertEquals(true, Primitive.LINES.isValidVertexCount(2));
		assertNotNull(Primitive.LINES.index());
	}

	@Test
	public void lineStrip() {
		assertEquals(2, Primitive.LINE_STRIP.size());
		assertEquals(true, Primitive.LINE_STRIP.isStrip());
		assertEquals(false, Primitive.LINE_STRIP.isNormalSupported());
		assertEquals(true, Primitive.LINE_STRIP.isValidVertexCount(0));
		assertEquals(false, Primitive.LINE_STRIP.isValidVertexCount(1));
		assertEquals(true, Primitive.LINE_STRIP.isValidVertexCount(2));
		assertNotNull(Primitive.LINE_STRIP.index());
	}

	@Test
	public void patch() {
		assertEquals(1, Primitive.PATCH.size());
		assertEquals(false, Primitive.PATCH.isStrip());
		assertEquals(false, Primitive.PATCH.isNormalSupported());
		assertEquals(true, Primitive.PATCH.isValidVertexCount(0));
		assertEquals(true, Primitive.PATCH.isValidVertexCount(1));
		assertEquals(true, Primitive.PATCH.isValidVertexCount(2));
		assertNull(Primitive.PATCH.index());
	}
}
