package org.sarge.jove.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.obj.ObjectMaterial.TextureMap;

public class ObjectMaterialTest {
	private ObjectMaterial mat;

	@BeforeEach
	public void before() {
		mat = new ObjectMaterial.Builder("mat")
			.illumination(1)
			.illumination(2)
			.colour(Colour.Type.AMBIENT, Colour.WHITE)
			.colour(Colour.Type.DIFFUSE, Colour.BLACK)
			.texture(TextureMap.SPECULAR, "filename")
			.build();
	}

	@Test
	public void constructor() {
		assertNotNull(mat);
		assertEquals("mat", mat.name());
	}

	@Test
	public void illumination() {
		assertEquals(Set.of(1, 2), mat.illumination());
	}

	@Test
	public void colours() {
		assertEquals(Colour.WHITE, mat.colours().get(Colour.Type.AMBIENT));
		assertEquals(Colour.BLACK, mat.colours().get(Colour.Type.DIFFUSE));
		assertEquals(null, mat.colours().get(Colour.Type.SPECULAR));
	}

	@Test
	public void textures() {
		assertEquals("filename", mat.textures().get(TextureMap.SPECULAR));
		assertEquals(null, mat.textures().get(TextureMap.SPECULAR_HIGHLIGHT));
	}
}
