package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class LightTest {
	private static final Colour COL = Colour.WHITE;
	private static final Vector DIR = new Vector();
	private static final Point POS = Point.ORIGIN;

	@Test
	public void ambient() {
		final Light ambient = Light.ambient(COL);
		checkLight(ambient, null, null);
	}

	@Test
	public void directional() {
		final Light directional = Light.directional(COL, DIR);
		checkLight(directional, null, DIR);
	}

	@Test
	public void point() {
		final Light point = Light.pointSource(COL, POS);
		checkLight(point, POS, null);
	}

	@Test
	public void spotLight() {
		final Light spotLight = Light.spotLight(COL, POS, DIR);
		checkLight(spotLight, POS, DIR);
	}
	
	private static void checkLight(Light light, Point pos, Vector dir) {
		assertNotNull(light);
		assertEquals(COL, light.getColour());
		assertEquals(Optional.ofNullable(pos), light.getPosition());
		assertEquals(Optional.ofNullable(dir), light.getDirection());
	}
}
