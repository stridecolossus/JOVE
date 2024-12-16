package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

class NativeMethodTest {
	interface MockLibrary {
		int abs(int n);
		void srand(int seed);
		double frexp(double x, NativeReference<Integer> exponent);
	}

	private NativeMethod.Builder builder;
	private TransformerRegistry registry;
	private SymbolLookup lookup;
	private Arena arena;

	@BeforeEach
	void before() {
		registry = new TransformerRegistry();
		registry.register(int.class, new PrimitiveNativeTransformer<>(ValueLayout.JAVA_INT));
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

	@DisplayName("A native method can be constructed for a void API method")
	@Test
	void voidMethod() throws Exception {
		final MemorySegment address = lookup.find("srand").orElseThrow();
		final NativeMethod method = builder
				.address(address)
				.parameter(int.class)
				.build();
		assertEquals(null, method.invoke(new Object[]{3}, arena));
	}

	@DisplayName("A native method can be constructed that uses supported by-reference parameters")
	@Test
	void reference() {
		registry.register(double.class, new PrimitiveNativeTransformer<>(ValueLayout.JAVA_DOUBLE));
		registry.register(NativeReference.class, new NativeReferenceTransformer());

		final MemorySegment address = lookup.find("frexp").orElseThrow();

		final NativeMethod method = builder
				.address(address)
				.returns(double.class)
				.parameter(double.class)
				.parameter(NativeReference.class, ParameterMode.REFERENCE)
				.build();

		final var factory = new NativeReference.Factory();
		final var exponent = factory.integer();
		assertEquals(0.5d, method.invoke(new Object[]{4, exponent}, arena));
		assertEquals(3, exponent.get());
	}

	// TODO
	@DisplayName("A native method returns a wrapper for the special case of a returned array")
	@Test
	void array() {
	}

	@DisplayName("The number of arguments supplied to a native method must match the expected parameter count")
	@Test
	void invalidArgumentCount() throws Exception {
		final MemorySegment address = lookup.find("srand").orElseThrow();
		final NativeMethod method = builder
				.address(address)
				.parameter(int.class)
				.build();

		assertThrows(IllegalArgumentException.class, () -> method.invoke(new Object[]{1, 2}, arena));
		assertThrows(IllegalArgumentException.class, () -> method.invoke(new Object[]{}, arena));
		assertThrows(IllegalArgumentException.class, () -> method.invoke(null, arena));
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
