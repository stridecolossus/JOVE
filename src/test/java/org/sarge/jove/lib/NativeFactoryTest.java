package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeFactoryTest {
	private SymbolLookup lookup;
	private NativeFactory factory;

	@BeforeEach
	void before() {
		final var registry = new NativeMapperRegistry();
		registry.add(new DefaultNativeMapper<>(int.class, ValueLayout.JAVA_INT));
		lookup = Linker.nativeLinker().defaultLookup();
		factory = new NativeFactory(registry);
	}

	@DisplayName("A proxy for a native API can be constructed by the factory")
	@Test
	void build() {
		@SuppressWarnings("unused")
		interface MockLibrary {
			int abs(int n);
			static void ignored() { /* Empty */ }
		}
		final MockLibrary lib = factory.build(lookup, MockLibrary.class);
		assertEquals(42, lib.abs(-42));
	}

	@DisplayName("A native API method must use supported parameter and return types")
	@Test
	void unsupported() {
		interface Unsupported {
			void doh(String s);
		}
		assertThrows(IllegalArgumentException.class, () -> factory.build(lookup, Unsupported.class));
	}

	@DisplayName("A native API must be defined by an interface")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> factory.build(lookup, Object.class));
	}
}
