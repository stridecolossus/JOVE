package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.*;

class NativeFactoryTest {
	interface MockLibrary {
		int abs(int n);
		static void ignored() { /* Empty */ }
	}

	private SymbolLookup lookup;
	private NativeFactory factory;

	@BeforeEach
	void before() {
		final var registry = new NativeMapperRegistry();
		registry.add(new PrimitiveNativeMapper<>(int.class));
		lookup = Linker.nativeLinker().defaultLookup();
		factory = new NativeFactory(registry);
	}

	@DisplayName("A proxy for a native API can be constructed")
	@Test
	void build() {
		final MockLibrary lib = factory.build(lookup, MockLibrary.class);
		assertEquals(42, lib.abs(-42));
	}

	@DisplayName("A return value handler can be configured all API methods")
	@Test
	void handler() {
		final var result = new AtomicReference<>();
		factory.setReturnHandler(result::set);

		final MockLibrary lib = factory.build(lookup, MockLibrary.class);
		lib.abs(-42);
		assertEquals(42, result.get());
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
