package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;
import java.util.function.Function;

import org.junit.jupiter.api.*;

class IdentityTransformerTest {
	private IdentityTransformer<Integer> transformer;

	@BeforeEach
	void before() {
		transformer = new IdentityTransformer<>(ValueLayout.JAVA_INT);
	}

	@Test
	void marshal() {
		assertEquals(42, transformer.marshal(42, null));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	void unmarshal() {
		final Function function = transformer.unmarshal();
		assertEquals(42, function.apply(42));
	}
}
