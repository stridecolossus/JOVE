package org.sarge.jove.foreign;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sarge.jove.common.*;
import org.sarge.jove.util.*;

class DefaultRegistryTest {
	private Registry registry;

	@BeforeEach
	void before() {
		registry = DefaultRegistry.create();
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

	@Test
	void string() {
		registry.transformer(String.class);
	}

	@Test
	void common() {
		registry.transformer(Handle.class);
		registry.transformer(NativeObject.class);
	}

	@Test
	void reference() {
		registry.transformer(NativeReference.class);
	}

	@Test
	void enumerations() {
		registry.transformer(MockEnum.class);
		registry.transformer(EnumMask.class);
	}

	@Test
	void structures() {
		registry.transformer(MockStructure.class);
	}
}
