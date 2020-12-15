package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

public class FaceParserTest {
	private FaceParser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		parser = new FaceParser();
		model = new ObjectModel();
		model.vertices().add(Point.ORIGIN);
		model.start();
	}

	@Test
	void parsePosition() {
		parser.parse(new String[]{"1", "1", "1"}, model);
		final Model result = model.current().build();
		assertEquals(3, result.count());
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION), result.layout());
		assertEquals(Primitive.TRIANGLES, result.primitive());
	}

	@Test
	void parsePositionTexture() {
		model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
		parser.parse(new String[]{"1/1", "1/1", "1/1"}, model);
		final Model result = model.current().build();
		assertEquals(3, result.count());
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE), result.layout());
		assertEquals(Primitive.TRIANGLES, result.primitive());
	}

	@Test
	void parsePositionTextureNormal() {
		model.normals().add(Vector.X_AXIS);
		model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
		parser.parse(new String[]{"1/1/1", "1/1/1", "1/1/1"}, model);
		final Model result = model.current().build();
		assertEquals(3, result.count());
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.NORMAL, Vertex.Component.TEXTURE_COORDINATE), result.layout());
		assertEquals(Primitive.TRIANGLES, result.primitive());
	}

	@Test
	void parseInvalidFace() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"1/2/3/4"}, model));
	}
}
