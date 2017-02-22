package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
		checkLight(ambient, false, false);
	}

	@Test
	public void directional() {
		final Light directional = Light.directional(COL, DIR);
		checkLight(directional, true, false);
	}

	@Test
	public void point() {
		final Light point = Light.point(COL, POS);
		checkLight(point, false, true);
	}

	@Test
	public void spotLight() {
		final Light spotLight = Light.spotLight(COL, DIR, POS);
		checkLight(spotLight, true, true);
	}
	
	private static void checkLight(Light light, boolean dir, boolean pos) {
		assertNotNull(light);
		assertEquals(COL, light.getColour());
		if(dir) {
			assertEquals(DIR, light.getDirection().get());
		}
		else {
			assertFalse(light.getDirection().isPresent());
		}
		if(pos) {
			assertEquals(POS, light.getPosition().get());
		}
		else {
			assertFalse(light.getPosition().isPresent());
		}
	}
}
