package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.obj.ObjectModel.ComponentList;

public class ObjectModelTest {
	private ObjectModel group;

	@BeforeEach
	void before() {
		group = new ObjectModel();
	}

	@Test
	void constructor() {
		assertNotNull(group.vertices());
		assertNotNull(group.normals());
		assertNotNull(group.coordinates());
		assertEquals(0, group.vertices().size());
		assertEquals(0, group.normals().size());
		assertEquals(0, group.coordinates().size());
		assertNotNull(group.builders());
		assertEquals(0, group.builders().count());
	}

	@Test
	void vertices() {
		final var vertices = group.vertices();
		vertices.add(Point.ORIGIN);
		assertEquals(1, group.vertices().size());
		assertEquals(Point.ORIGIN, vertices.get(1));
	}

	@Test
	void normals() {
		final var normals = group.normals();
		normals.add(Vector.X_AXIS);
		assertEquals(1, normals.size());
		assertEquals(Vector.X_AXIS, normals.get(1));
	}

	@Test
	void coordinates() {
		final var coords = group.coordinates();
		coords.add(Coordinate2D.BOTTOM_LEFT);
		assertEquals(1, coords.size());
		assertEquals(Coordinate2D.BOTTOM_LEFT, coords.get(1));
	}

	@Test
	void flip() {
		final var coords = group.coordinates();
		group.setFlipTextureCoordinates(true);
		coords.add(new Coordinate2D(1, 2));
		assertEquals(1, coords.size());
		assertEquals(new Coordinate2D(1, -2), coords.get(1));
	}

	@Test
	void start() {
		group.start();
		assertEquals(1, group.builders().count());
		assertNotNull(group.current());
	}

	@Test
	void startNullModel() {
		group = new ObjectModel(() -> null);
		assertThrows(NullPointerException.class, () -> group.start());
	}

	@Test
	void currentNotStarted() {
		assertThrows(IllegalStateException.class, () -> group.current());
	}

	@Test
	void init() {
		// Init model consisting of triangles with normals
		group.normals().add(Vector.X_AXIS);
		group.start();
		group.init(3);
		assertEquals(Primitive.TRIANGLES, group.current().primitive());

		// Add a triangle and check layout
		for(int n = 0; n < 3; ++n) {
			final Vertex vertex = new Vertex.Builder().position(Point.ORIGIN).normal(Vector.X_AXIS).build();
			group.add(vertex);
		}
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.NORMAL), group.current().build().layout());
	}

	@Test
	void initInvalidPrimitive() {
		group.start();
		assertThrows(UnsupportedOperationException.class, () -> group.init(0));
		assertThrows(UnsupportedOperationException.class, () -> group.init(4));
	}

	@Test
	void initFaceSizeMismatch() {
		group.start();
		group.init(2);
		assertThrows(IllegalStateException.class, () -> group.init(3));
	}

	@Test
	void add() {
		group.start();
		group.init(2);
		group.add(Vertex.of(Point.ORIGIN));
		group.add(Vertex.of(Point.ORIGIN));
		assertEquals(Primitive.LINES, group.current().primitive());
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION), group.current().build().layout());
	}

	@Test
	void addNotInitialised() {
		assertThrows(IllegalStateException.class, () -> group.add(Vertex.of(Point.ORIGIN)));
	}

	@Nested
	class ComponentListTests {
		private ComponentList<Object> list;
		private Object obj;

		@BeforeEach
		void before() {
			obj = new Object();
			list = new ComponentList<>();
			list.add(obj);
			list.add(new Object());
		}

		@Test
		void get() {
			assertEquals(obj, list.get(1));
		}

		@Test
		void getNegativeIndex() {
			assertEquals(obj, list.get(-2));
		}

		@Test
		void getInvalidZeroIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
		}

		@Test
		void getInvalidIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(-3));
		}
	}
}
