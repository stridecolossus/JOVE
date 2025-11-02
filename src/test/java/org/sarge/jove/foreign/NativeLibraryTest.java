package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;

class NativeLibraryTest {
	private interface MockInterface {
		int abs(int n);
	}

	private NativeLibrary.Builder builder;
	private SymbolLookup lookup;
	private Registry registry;

	@BeforeEach
	void before() {
		lookup = Linker.nativeLinker().defaultLookup();
		registry = new Registry();
		registry.register(int.class, new IdentityTransformer<>(ValueLayout.JAVA_INT));
		builder = new NativeLibrary.Builder(lookup, registry);
	}

	@DisplayName("A native library can be constructed for a given API")
	@Test
	void build() {
		final NativeLibrary lib = builder.build(List.of(MockInterface.class));
		final MockInterface api = lib.get();
		assertEquals(3, api.abs(-3));
	}

	@DisplayName("Validation can be applied to the return values of all methods in the library")
	@Test
	void handler() {
		final NativeLibrary lib = builder.build(List.of(MockInterface.class));
		final var listener = new AtomicReference<>();
		lib.handler(listener::set);

		final MockInterface api = lib.get();
		api.abs(42);
		assertEquals(42, listener.get());
	}

	@DisplayName("A native API must be expressed as an interface")
	@Test
	void concrete() {
		assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(Object.class)));
	}

	@DisplayName("All methods defined in the API must be present in the native library")
	@Test
	void unknown() {
		interface Unknown {
			void cobblers();
		}
		assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(Unknown.class)));
	}

	@DisplayName("The return type of a given native method must be supported")
	@Test
	void unsupportedReturnType() {
		interface Unsupported {
			String strerror(int code);
		}
		assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(Unsupported.class)));
	}

	@DisplayName("All parameters of a given native method must be supported types")
	@Test
	void unsupportedParameterType() {
		interface Unsupported {
			int strlen(String string);
		}
		assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(Unsupported.class)));
	}
}
