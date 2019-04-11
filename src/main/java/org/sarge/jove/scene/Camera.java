package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Rotation;
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
	private Vector dir = Vector.Z_AXIS;

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
	 * @see #direction()
	 */
	public void move(float dist) {
		move(dir.scale(-dist));
	}

	/**
	 * Moves the camera by the given distance in the current right axis.
	 * @param dist Distance to strafe
	 * @see #right()
	 */
	public void strafe(float dist) {
		move(right.scale(dist));
	}

	/**
	 * @return Camera view direction
	 */
	public Vector direction() {
		return dir.invert();
	}

	/**
	 * Sets the camera view direction.
	 * @param dir View direction (assumes normalized)
	 */
	public void direction(Vector dir) {
		this.dir = dir.invert();
		dirty();
	}

	/**
	 * Points the camera at the given location.
	 * @param pt Camera point-of-interest
	 */
	public void look(Point pt) {
		final Vector vec = Vector.of(pos, pt).normalize();
		direction(vec);
	}

	/**
	 * Rotates the camera.
	 * @param yaw		Horizontal angle
	 * @param pitch		Vertical angle
	 */
	public void rotate(float yaw, float pitch) {
		// http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-17-quaternions/
		// https://www.gamedev.net/articles/programming/math-and-physics/a-simple-quaternion-based-camera-r1997/

		// TODO - can probably just multiply X and Y rotations in one go
		final Quaternion qyaw = Quaternion.of(Rotation.of(up, yaw));
		final Quaternion qview = new Quaternion(0, dir.x, dir.y, dir.z);
		final Quaternion result = qyaw.multiply(qview).multiply(qyaw.conjugate());

//		// TODO - remove rotation
//		final Quaternion qyaw = Quaternion.of(Rotation.of(up, yaw));
//		final Quaternion qpitch = Quaternion.of(Rotation.of(right, pitch));
//		final Quaternion rot = qyaw.multiply(qpitch);

		System.out.print(yaw+" "+dir+" -> ");
//		dir = new Vector(rot.x, rot.y, rot.z);
		dir = new Vector(result.x, result.y, result.z).normalize();
		System.out.println(dir);
		dirty();


//		glm::quaternion rotation(glm::angleAxis(mVerticalAngle, glm::vec3(1.0f, 0.0f, 0.0f)));
//	    rotation = rotation * glm::angleAxis(mHorizontalAngle, glm::vec3(0.0f, 1.0f, 0.0f));

//		// TODO - quaternion and matrix multiply by tuple
//		final Point result = rot.matrix().multiply(new Point(dir));
//		System.out.println(dir+" -> "+result);
//		direction(new Vector(result).normalize());

//		final float cos = MathsUtil.cos(pitch);
//		final float x = MathsUtil.cos(yaw) * cos;
//		final float y = MathsUtil.sin(pitch);
//		final float z = MathsUtil.sin(yaw) * cos;
//		final Vector result = new Vector(x, y, z).normalize();
//		direction(result);
	}

//	protected void rotate(Vector axis, float angle) {
//		final Quaternion qaxis = Quaternion.of(axis, angle);
//		final Quaternion view = Quaternion.of(dir);
//		final Quaternion result = qaxis.multiply(view).multiply(qaxis.conjugate());
//		dir = result.vector();
//		dirty();
//	}

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
		// Determine right axis
		right = up.cross(dir).normalize();

		// Determine up axis
		final Vector y = dir.cross(right).normalize();

		// Calculate translation component
		final Vector trans = new Vector(-right.dot(pos), -y.dot(pos), -dir.dot(pos));

		// Build camera matrix
		matrix = new Matrix.Builder()
			.identity()
			.row(0, right)
			.row(1, y)
			.row(2, dir)
			.column(3, trans)
			.build();
	}
	// https://github.com/fynnfluegge/oreon-engine/blob/master/oreonengine/oe-core/src/main/java/org/oreon/core/scenegraph/Camera.java
	// http://www.songho.ca/opengl/gl_camera.html
}
