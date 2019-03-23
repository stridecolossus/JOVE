package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.BufferPropertyBinder;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.Material.Property;
import org.sarge.lib.util.AbstractObject;

/**
 * Model for a camera within a scene.
 * @author Sarge
 */
public class Camera extends AbstractObject {
	// Camera state
	private Point pos = Point.ORIGIN;
	private Vector dir = Vector.Z_AXIS.invert();

	// Axes
	private Vector up = Vector.Y_AXIS;
	private transient Vector right;

	// Matrix
	private transient Matrix matrix;
	private transient boolean dirty;

	/**
	 * Constructor.
	 */
	public Camera() {
		update();
	}

	/**
	 * @return Camera position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * Moves the camera to a new position.
	 * @param pos New position
	 */
	public void move(Point pos) {
		this.pos = notNull(pos);
		dirty();
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vec Movement vector
	 */
	public void move(Vector vec) {
		pos = pos.add(vec);
		dirty();
	}

	/**
	 * Moves the camera by the given distance in the current view direction.
	 * @param dist Distance to move
	 */
	public void move(float dist) {
		move(dir.scale(dist));
		dirty();
	}

	/**
	 * @return Camera view direction
	 */
	public Vector direction() {
		return dir;
	}

	/**
	 * Points the camera in the given direction.
	 * @param dir View direction
	 */
	public void point(Vector dir) {
		this.dir = notNull(dir);
		dirty();
	}

	/**
	 * Points the camera at the given location.
	 * @param pt Camera point-of-interest
	 */
	public void look(Point pt) {
		this.dir = Vector.of(pos, pt);
		dirty();
	}
	// TODO - could extend camera to have a specific POI, orbit, etc?

	/**
	 * @return Camera up axis
	 */
	public Vector up() {
		return up;
	}

	/**
	 * Sets the up axis of this camera.
	 * @param up Up axis
	 */
	public void up(Vector up) {
		this.up = notNull(up);
		dirty();
	}

	/**
	 * @return Camera right axis
	 */
	public Vector right() {
		return right;
	}

	/**
	 * @return Whether the camera has been modified
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Marks the camera matrix as dirty.
	 */
	private void dirty() {
		dirty = true;
	}

	/**
	 * @return Camera view matrix
	 */
	public Matrix matrix() {
		if(dirty) {
			update();
			dirty = false;
		}
		return matrix;
	}

	/**
	 * Creates a material property for the view matrix of this camera.
	 * @return View matrix property
	 */
	public Material.Property viewMatrixProperty() {
		return property(BufferPropertyBinder.matrix(this::matrix));
	}

	/**
	 * Creates a material property for the position of this camera.
	 * @return Camera position property
	 */
	public Material.Property positionProperty() {
		return property(BufferPropertyBinder.tuple(this::position));
	}

	/**
	 * Creates a material property for the view direction of this camera.
	 * @return Camera view direction property
	 */
	public Material.Property directionProperty() {
		return property(BufferPropertyBinder.tuple(this::direction));
	}

	/**
	 * Creates a per-frame material property binder.
	 */
	private static Material.Property property(BufferPropertyBinder binder) {
		return new Material.Property(binder, Property.Policy.FRAME);
	}

	/**
	 * Updates the camera axes and matrix.
	 */
	private void update() {
		// Derive camera axes
		final Vector z = dir.invert();
		right = up.cross(z).normalize();
		final Vector y = z.cross(right).normalize();

		// Calculate camera translation
		final Vector trans = new Vector(right.dot(pos),	y.dot(pos),	z.dot(pos));

		// Construct camera matrix
		matrix = new Matrix.Builder()
			.identity()
			.row(0, right)
			.row(1, y)
			.row(2, z)
			.column(3, trans.invert())
			.build();
	}
}
