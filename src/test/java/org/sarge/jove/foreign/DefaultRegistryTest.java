package org.sarge.jove.foreign;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.util.*;

class DefaultRegistryTest {
	private Registry registry;

	@BeforeEach
	void before() {
		registry = DefaultRegistry.create();
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
