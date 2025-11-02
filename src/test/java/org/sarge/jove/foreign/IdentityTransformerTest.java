package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class IdentityTransformerTest {
	private IdentityTransformer<Integer> transformer;

	@BeforeEach
	void before() {
		transformer = new IdentityTransformer<>(ValueLayout.JAVA_INT);
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.JAVA_INT, transformer.layout());
	}

	@Test
	void marshal() {
		assertEquals(3, transformer.marshal(3, null));
	}

	@Test
	void empty() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.empty());
	}

	@Test
	void unmarshal() {
		assertEquals(4, transformer.unmarshal().apply(4));
	}

	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}

	@Nested
	class PrimitiveTransformers {
    	private Registry registry;

    	@BeforeEach
    	void before() {
    		registry = new Registry();
    		IdentityTransformer.primitives(registry);
    	}

    	static ValueLayout[] primitives() {
    		return new ValueLayout[] {
    				ValueLayout.JAVA_BOOLEAN,
    				ValueLayout.JAVA_BYTE,
    				ValueLayout.JAVA_CHAR,
    				ValueLayout.JAVA_SHORT,
    				ValueLayout.JAVA_INT,
    				ValueLayout.JAVA_LONG,
    				ValueLayout.JAVA_FLOAT,
    				ValueLayout.JAVA_DOUBLE,
    		};
    	}

    	@ParameterizedTest
    	@MethodSource
    	void primitives(ValueLayout layout) {
			final var transformer = registry.transformer(layout.carrier()).get();
    		assertEquals(layout, transformer.layout());
    	}
    }
}
