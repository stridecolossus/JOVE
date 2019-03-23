package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrimitiveTest {
	@Test
	public void triangle() {
		assertEquals(3, Primitive.TRIANGLE.size());
		assertEquals(false, Primitive.TRIANGLE.isStrip());
		assertEquals(true, Primitive.TRIANGLE.hasNormals());
		assertEquals(true, Primitive.TRIANGLE.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLE.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLE.isValidVertexCount(3));
		assertEquals(false, Primitive.TRIANGLE.isValidVertexCount(4));
		assertEquals(true, Primitive.TRIANGLE.isValidVertexCount(6));
	}

	@Test
	public void triangleStrip() {
		assertEquals(3, Primitive.TRIANGLE_STRIP.size());
		assertEquals(true, Primitive.TRIANGLE_STRIP.isStrip());
		assertEquals(true, Primitive.TRIANGLE_STRIP.hasNormals());
		assertEquals(true, Primitive.TRIANGLE_STRIP.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLE_STRIP.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLE_STRIP.isValidVertexCount(3));
		assertEquals(true, Primitive.TRIANGLE_STRIP.isValidVertexCount(4));
	}

	@Test
	public void quad() {
		assertEquals(4, Primitive.QUAD.size());
		assertEquals(false, Primitive.QUAD.isStrip());
		assertEquals(true, Primitive.QUAD.hasNormals());
		assertEquals(true, Primitive.QUAD.isValidVertexCount(0));
		assertEquals(false, Primitive.QUAD.isValidVertexCount(2));
		assertEquals(true, Primitive.QUAD.isValidVertexCount(4));
		assertEquals(false, Primitive.QUAD.isValidVertexCount(5));
		assertEquals(true, Primitive.QUAD.isValidVertexCount(8));
	}

	@Test
	public void point() {
		assertEquals(1, Primitive.POINT.size());
		assertEquals(false, Primitive.POINT.isStrip());
		assertEquals(false, Primitive.POINT.hasNormals());
		assertEquals(true, Primitive.POINT.isValidVertexCount(0));
		assertEquals(true, Primitive.POINT.isValidVertexCount(1));
		assertEquals(true, Primitive.POINT.isValidVertexCount(2));
	}

	@Test
	public void line() {
		assertEquals(2, Primitive.LINE.size());
		assertEquals(false, Primitive.LINE.isStrip());
		assertEquals(false, Primitive.LINE.hasNormals());
		assertEquals(true, Primitive.LINE.isValidVertexCount(0));
		assertEquals(false, Primitive.LINE.isValidVertexCount(1));
		assertEquals(true, Primitive.LINE.isValidVertexCount(0));
		assertEquals(true, Primitive.LINE.isValidVertexCount(2));
	}

	@Test
	public void lineStrip() {
		assertEquals(2, Primitive.LINE_STRIP.size());
		assertEquals(true, Primitive.LINE_STRIP.isStrip());
		assertEquals(false, Primitive.LINE_STRIP.hasNormals());
		assertEquals(true, Primitive.LINE_STRIP.isValidVertexCount(0));
		assertEquals(false, Primitive.LINE_STRIP.isValidVertexCount(1));
		assertEquals(true, Primitive.LINE_STRIP.isValidVertexCount(2));
	}
}
