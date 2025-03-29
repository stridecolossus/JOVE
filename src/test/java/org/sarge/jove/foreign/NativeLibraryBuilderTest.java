package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;

class NativeLibraryBuilderTest {
	@SuppressWarnings("unused")
	private interface MockInterface {
		int abs(int n);

		static void ignored() {
			// Empty
		}
	}

	private NativeLibraryBuilder builder;
	private NativeRegistry registry;

	@BeforeEach
	void before() {
		final SymbolLookup lookup = Linker.nativeLinker().defaultLookup();
		registry = new NativeRegistry();
		registry.add(int.class, new IdentityNativeTransformer<>(ValueLayout.JAVA_INT));
		builder = new NativeLibraryBuilder(lookup, registry);
	}

	@Test
	void build() {
		final MockInterface proxy = builder.build(MockInterface.class);
		assertEquals(3, proxy.abs(-3));
	}

	@Test
	void handler() {
		final var listener = new AtomicInteger();
		builder.setReturnValueHandler(listener::set);

		final MockInterface proxy = builder.build(MockInterface.class);
		proxy.abs(42);
		assertEquals(42, listener.get());
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> builder.build(String.class));
	}
}
