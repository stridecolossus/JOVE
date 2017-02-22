package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.MatrixBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

public class CameraTest {
	private Camera cam;

	@Before
	public void before() {
		cam = new Camera();
	}

	@Test
	public void constructor() {
		assertEquals( new Point( 0, 0, 5 ), cam.getPosition() );
		assertEquals( Point.ORIGIN, cam.getTarget() );
		assertEquals( Vector.Z_AXIS.invert(), cam.getDirection() );
		assertEquals( Vector.Y_AXIS, cam.getUpDirection() );
	}

	@Test
	public void getViewMatrix() {
		final MatrixBuilder expected = new MatrixBuilder( 4 );
		expected.set( 2, 3, -5 );
		assertEquals( expected, cam.getViewMatrix() );
	}

	@Test
	public void setPosition() {
		final Point pos = new Point( 1, 2, 3 );
		cam.setPosition( pos );
		assertEquals( pos, cam.getPosition() );
		assertNotNull( cam.getViewMatrix() );
	}

	@Test
	public void setTarget() {
		final Point target = new Point( 5, 0, 5 );
		cam.setTarget( target );
		assertEquals( target, cam.getTarget() );
		assertEquals( Vector.X_AXIS, cam.getDirection() );
	}

	@Test
	public void setUpDirection() {
		final Vector up = new Vector( 1, 2, 3 ).normalize();
		cam.setUpDirection( up );
		assertEquals( up, cam.getUpDirection() );
		assertNotNull( cam.getViewMatrix() );
	}

	@Test
	public void moveVector() {
		cam.move( new Vector( 1, 2, 3 ) );
		assertEquals( new Vector( 1, 2, 8 ), cam.getPosition() );
	}

	@Test
	public void moveAmount() {
		cam.move( 3 );
		assertEquals( new Point( 0, 0, 5 - 3 ), cam.getPosition() );
	}

	@Test
	public void strafe() {
		cam.strafe( 3 );
		assertEquals( new Point( 3, 0, 5 ), cam.getPosition() );
	}

	@Test
	public void rotate() {
		final Quaternion rot = new Quaternion( Vector.Y_AXIS, MathsUtil.toRadians( 90 ) );
		cam.rotate( rot );
		assertEquals( new Vector( -1, 0, 0 ), cam.getDirection() );
	}

	@Test
	public void orbit() {
		final Quaternion rot = new Quaternion( Vector.Y_AXIS, MathsUtil.toRadians( 90 ) );
		cam.orbit( rot );
		assertEquals( Point.ORIGIN, cam.getTarget() );
		assertEquals( new Point( 5, 0, 0 ), cam.getPosition() );
		assertEquals( new Vector( -1, 0, 0 ), cam.getDirection() );
	}
}
