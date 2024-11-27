package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeMethodTest {
	interface MockLibrary {
		int abs(int n);
		void srand(int seed);
	}

	private NativeMethod.Builder builder;
	private SymbolLookup lookup;
	private Arena arena;

	@BeforeEach
	void before() {
		final var mapper = new PrimitiveNativeTransformer<>(int.class);
		final var registry = new TransformerRegistry();
		registry.add(mapper);
		arena = Arena.ofAuto();
		lookup = Linker.nativeLinker().defaultLookup();
		builder = new NativeMethod.Builder(registry);
	}

	@DisplayName("A native method can be constructed for an API method")
	@Test
	void build() throws Exception {
		final MemorySegment address = lookup.find("abs").orElseThrow();
		final NativeMethod method = builder
				.address(address)
				.returns(int.class)
				.parameter(int.class)
				.build();
		assertEquals(2, method.invoke(new Object[]{-2}, arena));
	}

	@DisplayName("A native method can be constructed for an API method without a return value")
	@Test
	void voidMethod() throws Exception {
		final MemorySegment address = lookup.find("srand").orElseThrow();
		final NativeMethod method = builder
				.address(address)
				.parameter(int.class)
				.build();
		assertEquals(null, method.invoke(new Object[]{3}, arena));
	}

	@DisplayName("A native method cannot be constructed for an unsupported parameter type")
	@Test
	void unsupportedParameter() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> builder.parameter(String.class));
	}

	@DisplayName("A native method cannot be constructed for an unsupported return type")
	@Test
	void unsupportedReturnType() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> builder.returns(String.class));
	}
}
