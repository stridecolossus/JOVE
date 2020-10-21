package org.sarge.jove.scene;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material;

public class SceneTest {
	private Scene scene;
	private Viewport viewport;

	@BeforeEach
	public void before() {
		final Rectangle rect = new Rectangle(new Coordinate(0, 0), new Dimensions(640, 480));
		viewport = new Viewport(rect, 1, 100, Projection.DEFAULT);
		scene = new Scene(viewport);
	}

	@Test
	public void constructor() {
		assertNotNull(scene.camera());
		assertNotNull(scene.frustum());
		assertNotNull(scene.root());
		assertEquals(viewport, scene.viewport());
		assertEquals(Optional.of(Colour.BLACK), scene.clear());
		assertEquals(viewport.matrix(), scene.projection());
	}

	@Test
	public void setRootNode() {
		final Node root = new Node("root");
		scene.root(root);
		assertEquals(root, scene.root());
	}

	@Test
	public void setClearColour() {
		scene.clear(Colour.WHITE);
		assertEquals(Optional.of(Colour.WHITE), scene.clear());
	}

	@Test
	public void setClearColourNone() {
		scene.clear(null);
		assertEquals(Optional.empty(), scene.clear());
	}

	@Test
	public void setViewport() {
		final Frustum prev = scene.frustum();
		scene.viewport(viewport);
		assertEquals(viewport, scene.viewport());
		assertNotEquals(prev, scene.frustum());
	}

	@Test
	public void frustum() {
		// Lookup frustum planes
		final Frustum frustum = scene.frustum();
		final Plane[] planes = frustum.planes();
		assertNotNull(planes);
		assertEquals(6, planes.length);

		// Check planes
		assertEquals(new Plane(Vector.Z_AXIS.invert(), -1), planes[0]);
		assertEquals(new Plane(Vector.Z_AXIS, 100), planes[1]);
//		assertEquals(new Plane(Vector.Y_AXIS.invert(), 1), planes[2]);
//		assertEquals(new Plane(Vector.Y_AXIS, -1), planes[3]);
//		assertEquals(new Plane(Vector.X_AXIS, -1), planes[4]);
//		assertEquals(new Plane(Vector.X_AXIS.invert(), 1), planes[5]);
		// TODO
	}

	@Test
	public void projectionProperty() {
		final Material.Property prop = scene.projectionMatrixProperty();
		assertNotNull(prop);
	}
}
