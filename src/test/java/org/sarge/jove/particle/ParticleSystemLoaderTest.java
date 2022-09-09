package org.sarge.jove.particle;

import java.io.IOException;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.util.Element;

public class ParticleSystemLoaderTest {
	private ParticleSystemLoader loader;

	@BeforeEach
	void before() {
		loader = new ParticleSystemLoader(new Randomiser());
	}

	@Test
	void load() throws IOException {


		loader.load(Element.of("root"));

	}
}
