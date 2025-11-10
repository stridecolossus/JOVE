package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		model.positions().add(Point.ORIGIN);
		model.start();
	}

	@Test
	void add() {
		final var vertex = new Vertex(Point.ORIGIN);
		model.add(vertex);
	}

	@Test
	void models() {
		// Add a triangle
		final var vertex = new Vertex(Point.ORIGIN);
		model.add(vertex);
		model.add(vertex);
		model.add(vertex);

		// Build model
		final List<Mesh> list = model.build();
		assertEquals(1, list.size());

		// Check model
		final Mesh mesh = list.get(0);
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(3, mesh.count());
	}
}
