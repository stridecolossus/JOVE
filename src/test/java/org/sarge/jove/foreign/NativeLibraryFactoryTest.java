package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;

class NativeLibraryFactoryTest {
	private interface MockInterface {
		int abs(int n);
	}

	private NativeLibraryFactory factory;
	private SymbolLookup lookup;
	private Registry registry;

	@BeforeEach
	void before() {
		lookup = Linker.nativeLinker().defaultLookup();
		registry = new Registry();
		registry.add(int.class, new PrimitiveTransformer<>(ValueLayout.JAVA_INT));
		factory = new NativeLibraryFactory(lookup, registry);
	}

	@DisplayName("A native library can be constructed for a given API")
	@Test
	void build() {
		final var library = (MockInterface) factory.build(List.of(MockInterface.class));
		assertEquals(3, library.abs(-3));
	}

	@DisplayName("Validation can be applied to the return values of all methods in the library")
	@Test
	void handler() {
		final var listener = new AtomicReference<>();
		factory.handler(listener::set);

		final var library = (MockInterface) factory.build(List.of(MockInterface.class));
		library.abs(42);
		assertEquals(42, listener.get());
	}

	@DisplayName("A native API must be expressed as an interface")
	@Test
	void concrete() {
		assertThrows(IllegalArgumentException.class, () -> factory.build(List.of(Object.class)));
	}

	@DisplayName("All methods defined in the API must be present in the native library")
	@Test
	void unknown() {
		interface Unknown {
			void cobblers();
		}
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.build(List.of(Unknown.class)));
		assertTrue(e.getMessage().startsWith("Unknown native method"));
	}

	@DisplayName("The return type of a given native method must be supported")
	@Test
	void unsupportedReturnType() {
		interface Unsupported {
			String strerror(int code);
		}
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.build(List.of(Unsupported.class)));
		assertTrue(e.getMessage().startsWith("Unsupported return type"));
	}

	@DisplayName("All parameters of a given native method must be supported types")
	@Test
	void unsupportedParameterType() {
		interface Unsupported {
			int strlen(String string);
		}
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.build(List.of(Unsupported.class)));
		assertTrue(e.getMessage().startsWith("Unsupported parameter type"));
	}
}
