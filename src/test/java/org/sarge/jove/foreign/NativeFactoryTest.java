package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;

class NativeFactoryTest {
	interface MockLibrary {
		int abs(int n);
		static void ignored() { /* Empty */ }
	}

	private SymbolLookup lookup;
	private TransformerRegistry registry;
	private NativeFactory factory;

	@BeforeEach
	void before() {
		registry = new TransformerRegistry();
		lookup = Linker.nativeLinker().defaultLookup();
		factory = new NativeFactory(registry);
	}

	private void register() {
		registry.register(int.class, new PrimitiveNativeTransformer<>(ValueLayout.JAVA_INT));
	}

	@DisplayName("A proxy for a native library can be constructed from a Java interface defining the API")
	@Test
	void build() {
		register();
		final MockLibrary lib = factory.build(lookup, MockLibrary.class);
		assertEquals(42, lib.abs(-42));
	}

	@DisplayName("A return value handler can be configured all API methods")
	@Test
	void handler() {
		final var result = new AtomicReference<>();
		factory.setReturnHandler(result::set);
		register();

		final MockLibrary lib = factory.build(lookup, MockLibrary.class);
		lib.abs(-42);
		assertEquals(42, result.get());
	}

	@DisplayName("A native method declared in the API must be present in the library")
	@Test
	void unknown() {
		interface Unknown {
			void unknown();
		}
		register();
		assertThrows(IllegalArgumentException.class, () -> factory.build(lookup, Unknown.class));
	}

	@DisplayName("A native API method must use supported parameter and return types")
	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> factory.build(lookup, MockLibrary.class));
	}

	@DisplayName("A native API must be defined by an interface")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> factory.build(lookup, Object.class));
	}
}
