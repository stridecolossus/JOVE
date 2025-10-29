package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;

class NativeLibraryFactoryTest {
	@SuppressWarnings("unused")
	private interface MockInterface {
		int abs(int n);
		int strlen(String str);

		static void ignored() {
			// Empty
		}
	}

	private NativeLibraryFactory factory;
	private SymbolLookup lookup;
	private Registry registry;

	@BeforeEach
	void before() {
		lookup = Linker.nativeLinker().defaultLookup();
		registry = new Registry();
		registry.register(int.class, new IdentityTransformer<>(ValueLayout.JAVA_INT));
		registry.register(String.class, new StringTransformer());
		factory = new NativeLibraryFactory(lookup, registry);
	}

	@DisplayName("A proxy implementation can be constructed for a given native API")
	@Test
	void build() {
		final MockInterface proxy = factory.build(MockInterface.class);
		assertEquals(3, proxy.abs(-3));
		assertEquals(6, proxy.strlen("string"));
	}

	@DisplayName("Validation can be applied to the return values of all methods in the API")
	@Test
	void handler() {
		final var listener = new AtomicReference<>();
		factory.setReturnValueHandler(listener::set);

		final MockInterface proxy = factory.build(MockInterface.class);
		proxy.abs(42);
		assertEquals(42, listener.get());
	}

	@DisplayName("A native API must be expressed as an interface")
	@Test
	void concrete() {
		assertThrows(IllegalArgumentException.class, () -> factory.build(Object.class));
	}

	@DisplayName("All methods defined in the API must be present in the native library")
	@Test
	void unknown() {
		lookup = _ -> Optional.empty();
		factory = new NativeLibraryFactory(lookup, registry);
		assertThrows(IllegalArgumentException.class, () -> factory.build(MockInterface.class));
	}

	@DisplayName("The return type of a given native method must be supported")
	@Test
	void unsupportedReturnType() {
		registry = new Registry();
		registry.register(String.class, new StringTransformer());
		factory = new NativeLibraryFactory(lookup, registry);
		assertThrows(IllegalArgumentException.class, () -> factory.build(MockInterface.class));
	}

	@DisplayName("All parameters of a given native method must be supported types")
	@Test
	void unsupportedParameterType() {
		factory = new NativeLibraryFactory(lookup, new Registry());
		assertThrows(IllegalArgumentException.class, () -> factory.build(MockInterface.class));
	}
}
