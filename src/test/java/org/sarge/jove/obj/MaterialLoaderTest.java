package org.sarge.jove.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.obj.DefaultObjectLoader.Parser;
import org.sarge.jove.obj.ObjectMaterial.TextureMap;

public class MaterialLoaderTest {
	private ObjectMaterial.Builder mat;

	@BeforeEach
	public void before() {
		mat = mock(ObjectMaterial.Builder.class);
	}

	@Nested
	class ColourParserTests {
		private Parser<ObjectMaterial.Builder> parser;

		@BeforeEach
		public void before() {
			parser = MaterialLoader.colour(Colour.Type.AMBIENT);
		}

		@Test
		public void parse() {
			parser.parse(new String[]{"Ka", "0.1", "0.2", "0.3"}, mat);
			verify(mat).colour(Colour.Type.AMBIENT, new Colour(0.1f, 0.2f, 0.3f, 1f));
		}
	}

	@Nested
	class TextureParserTests {
		private Parser<ObjectMaterial.Builder> parser;

		@BeforeEach
		public void before() {
			parser = MaterialLoader.texture(TextureMap.AMBIENT);
		}

		@Test
		public void parse() {
			parser.parse(new String[]{"map_Ka", "filename"}, mat);
			verify(mat).texture(TextureMap.AMBIENT, "filename");
		}
	}

	@Nested
	class IlluminationParserTests {
		@Test
		public void parse() {
			MaterialLoader.ILLUMINATION.parse(new String[]{"illum", "42"}, mat);
			verify(mat).illumination(42);
		}
	}
}
