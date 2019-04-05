package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.BufferPropertyBinder;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.Material.Property;

/**
 * A <i>scene</i> comprises a scene-graph and viewport.
 * @author Sarge
 */
public class Scene {
	// Camera
	private final Camera cam = new Camera();
	private transient Frustum frustum;

	// Viewport
	private Viewport viewport;
	private transient Matrix projection;

	// Scene
	private Optional<Colour> clear;
	private Node root;

	/**
	 * Constructor.
	 * @param viewport Scene viewport
	 */
	public Scene(Viewport viewport) {
		clear(Colour.BLACK);
		root(new Node("root"));
		viewport(viewport);
	}

	/**
	 * @return Camera for this scene
	 */
	public Camera camera() {
		return cam;
	}

	/**
	 * @return View frustum
	 */
	public Frustum frustum() {
		// TODO if(cam.isDirty()) {
			build();
		//}
		return frustum;
	}

	/**
	 * @return Viewport clear colour for this scene (default is {@link Colour#BLACK})
	 */
	public Optional<Colour> clear() {
		return clear;
	}

	/**
	 * Sets the viewport clear colour
	 * @param clear Clear colour (optional)
	 */
	public void clear(Colour clear) {
		this.clear = Optional.ofNullable(clear);
	}

	/**
	 * @return Scene-graph root node
	 */
	public Node root() {
		return root;
	}

	/**
	 * Sets the scene-graph root node.
	 * @param root Root node
	 */
	public void root(Node root) {
		this.root = notNull(root);
	}

	/**
	 * @return Scene viewport
	 */
	public Viewport viewport() {
		return viewport;
	}

	/**
	 * Sets the scene viewport.
	 * @param viewport Viewport
	 */
	public void viewport(Viewport viewport) {
		this.viewport = notNull(viewport);
		this.projection = viewport.matrix();
		build();
	}

	/**
	 * @return Projection matrix for this scene
	 */
	public Matrix projection() {
		return projection;
	}

	/**
	 * Creates a material property for the projection matrix of this scene.
	 * @return Projection matrix material projection
	 */
	public Material.Property projectionMatrixProperty() {
		return new Material.Property(BufferPropertyBinder.matrix(() -> projection), Property.Policy.MANUAL);
	}

	/**
	 * Builds the view frustum for this scene.
	 */
	private void build() {
		// Retrieve the frustum inputs
		// http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-implementation/
		final Plane[] planes = new Plane[6];
		final Point pos = cam.position();
		final Vector dir = cam.direction();
		final Vector up = cam.up();
		final Vector right = cam.right();

		// Calculate centre of near and far planes
		final Point nc = pos.add(dir.scale(viewport.near()));
		final Point fc = pos.add(dir.scale(viewport.far()));

		// Build near plane
		planes[0] = Plane.of(dir, nc);

		// Build far plane
		planes[1] = Plane.of(dir.invert(), fc);

		// Calculate top-left corner vector
		final Point top = nc.add(up.scale(viewport.height())).add(right.scale(-viewport.width()));
		final Vector tv = Vector.of(nc, top).normalize();

		// Calculate bottom-right corner vector
		final Point bottom = nc.add(up.scale(-viewport.height())).add(right.scale(viewport.width()));
		final Vector bv = Vector.of(nc, bottom).normalize();

		// Top plane
		planes[2] = Plane.of(tv.cross(right), top);

		// Bottom plane
		planes[3] = Plane.of(right.cross(tv), bottom);

		// Left plane
		planes[4] = Plane.of(bv.cross(up), top);

		// Right plane
		planes[5] = Plane.of(up.cross(bv), bottom);

		// Create frustum volume
		frustum = new Frustum(planes);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
