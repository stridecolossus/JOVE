package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class GenerationPolicyTest {
	private float elapsed;

	@DisplayName("The NONE policy does not generate particles")
	@Test
	void none() {
		assertEquals(0, GenerationPolicy.NONE.count(0, elapsed));
		assertEquals(0, GenerationPolicy.NONE.count(1, elapsed));
	}

	@DisplayName("A fixed policy maintains a constant number of particles")
	@Test
	void fixed() {
		final GenerationPolicy policy = GenerationPolicy.fixed(2);
		assertEquals(2, policy.count(0, elapsed));
		assertEquals(1, policy.count(1, elapsed));
		assertEquals(0, policy.count(2, elapsed));
	}

//	@Nested
//	class LoaderTests {
//		@Test
//		void none() {
//			assertEquals(GenerationPolicy.NONE, GenerationPolicy.LOADER.load(Element.of("none")));
//		}
//
//		//@Test
//		void fixed() {
//			final Element root = new Element("fixed", Map.of(), "3");
//			final var policy = GenerationPolicy.LOADER.load(root);
//			assertEquals(3, policy.count(0, elapsed));
//		}
//	}
}
