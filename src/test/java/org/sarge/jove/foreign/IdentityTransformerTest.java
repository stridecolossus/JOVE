package org.sarge.jove.foreign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class IdentityTransformerTest {
	private Registry registry;

	@BeforeEach
	void before() {
		registry = new Registry();
		IdentityTransformer.primitives(registry);
	}

	static Class<?>[] primitives() {
		return new Class<?>[] {
    			boolean.class,
    			byte.class,
    			char.class,
    			short.class,
    			int.class,
    			long.class,
    			float.class,
    			double.class
		};
	}

	@ParameterizedTest
	@MethodSource
	void primitives(Class<?> type) {
		registry.transformer(type);
	}
}
