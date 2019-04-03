package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrimitiveTest {
	@Test
	public void triangle() {
		assertEquals(3, Primitive.TRIANGLE_LIST.size());
		assertEquals(false, Primitive.TRIANGLE_LIST.isStrip());
		assertEquals(true, Primitive.TRIANGLE_LIST.hasNormals());
		assertEquals(true, Primitive.TRIANGLE_LIST.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLE_LIST.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLE_LIST.isValidVertexCount(3));
		assertEquals(false, Primitive.TRIANGLE_LIST.isValidVertexCount(4));
		assertEquals(true, Primitive.TRIANGLE_LIST.isValidVertexCount(6));
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
	public void triangleFan() {
		assertEquals(3, Primitive.TRIANGLE_FAN.size());
		assertEquals(true, Primitive.TRIANGLE_FAN.isStrip());
		assertEquals(true, Primitive.TRIANGLE_FAN.hasNormals());
		assertEquals(true, Primitive.TRIANGLE_FAN.isValidVertexCount(0));
		assertEquals(false, Primitive.TRIANGLE_FAN.isValidVertexCount(2));
		assertEquals(true, Primitive.TRIANGLE_FAN.isValidVertexCount(3));
		assertEquals(true, Primitive.TRIANGLE_FAN.isValidVertexCount(4));
	}

	@Test
	public void point() {
		assertEquals(1, Primitive.POINT_LIST.size());
		assertEquals(false, Primitive.POINT_LIST.isStrip());
		assertEquals(false, Primitive.POINT_LIST.hasNormals());
		assertEquals(true, Primitive.POINT_LIST.isValidVertexCount(0));
		assertEquals(true, Primitive.POINT_LIST.isValidVertexCount(1));
		assertEquals(true, Primitive.POINT_LIST.isValidVertexCount(2));
	}

	@Test
	public void line() {
		assertEquals(2, Primitive.LINE_LIST.size());
		assertEquals(false, Primitive.LINE_LIST.isStrip());
		assertEquals(false, Primitive.LINE_LIST.hasNormals());
		assertEquals(true, Primitive.LINE_LIST.isValidVertexCount(0));
		assertEquals(false, Primitive.LINE_LIST.isValidVertexCount(1));
		assertEquals(true, Primitive.LINE_LIST.isValidVertexCount(0));
		assertEquals(true, Primitive.LINE_LIST.isValidVertexCount(2));
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
