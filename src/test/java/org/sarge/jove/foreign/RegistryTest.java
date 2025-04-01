package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleTransformer;
import org.sarge.jove.util.*;

class RegistryTest {
	private Registry registry;

	@BeforeEach
	void before() {
		registry = new Registry();
	}

	@Test
	void add() {
		final var transformer = new Primitive(ValueLayout.JAVA_INT);
		registry.add(int.class, transformer);
		assertEquals(transformer, registry.get(int.class));
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> registry.get(int.class));
	}

	@Test
	void supertype() {
		final Transformer transformer = new Primitive(ValueLayout.JAVA_INT);
		registry.add(Number.class, transformer);
		assertEquals(transformer, registry.get(Integer.class));
	}

	@Test
	void array() {
		registry.add(Handle.class, new HandleTransformer());
		assertEquals(ValueLayout.ADDRESS, registry.get(Handle[].class).layout());
	}

	@Test
	void enumeration() {
		assertEquals(ValueLayout.JAVA_INT, registry.get(MockEnum.class).layout());
	}

	@Test
	void structure() {
		final var structure = new MockStructure();
		registry.add(int.class, new Primitive(ValueLayout.JAVA_INT));
		assertEquals(structure.layout(), registry.get(MockStructure.class).layout());
	}

	private static List<Class<?>> create() {
		return List.of(
				String.class,
				Handle.class,
				NativeObject.class,
				EnumMask.class,
				NativeReference.class
		);
	}

	@ParameterizedTest
	@MethodSource
	void create(Class<?> type) {
		Registry.create().get(type);
	}

	private static List<Class<?>> primitive() {
		return List.of(
				boolean.class,
				byte.class,
				char.class,
				short.class,
				int.class,
				long.class,
				float.class,
				double.class
		);
	}

	@ParameterizedTest
	@MethodSource
	void primitive(Class<?> type) {
		Registry.create().get(type);
	}
}
