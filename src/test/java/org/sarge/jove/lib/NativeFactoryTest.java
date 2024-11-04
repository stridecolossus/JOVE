package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeFactoryTest {
	private SymbolLookup lookup;
	private NativeFactory factory;

	@BeforeEach
	void before() {
		lookup = Linker.nativeLinker().defaultLookup();
		factory = new NativeFactory();
	}

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

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> factory.build(lookup, Object.class));
	}
}
