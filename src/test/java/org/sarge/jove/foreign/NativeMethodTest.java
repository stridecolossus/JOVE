package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeMethod.*;

class NativeMethodTest {
	private IdentityTransformer<Integer> integer;
	private NativeParameter parameter;

	@BeforeEach
	void before() {
		integer = new IdentityTransformer<>(JAVA_INT);
		parameter = new NativeParameter(integer);
	}

	@DisplayName("A native method can be invoked")
	@Test
	void invoke() {
		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class));
		final NativeMethod method = new NativeMethod(handle, null, List.of());
		assertEquals(null, method.invoke(null));
	}

	@DisplayName("A native method can optionally have a return value")
	@Test
	void returns() {
		final MethodHandle handle = MethodHandles.constant(int.class, 42);
		final NativeMethod method = new NativeMethod(handle, Function.identity(), List.of());
		assertEquals(42, method.invoke(null));
	}

	@DisplayName("A native method must specify a return transformer for a method with a return value")
	@Test
	void returnsRequiresTransformer() {
		final MethodHandle handle = MethodHandles.zero(int.class);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, null, List.of()));
	}

	@DisplayName("A native method cannot configure a return transformer for a method without a return value")
	@Test
	void returnsVoidTransformer() {
		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class));
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, Function.identity(), List.of()));
	}

	@DisplayName("A native method can have none-or-more parameters")
	@Test
	void parameter() {
		//final MethodHandle handle = MethodHandles.identity(int.class);
		final MethodHandle handle = MethodHandles.identity(int.class);
		final NativeMethod method = new NativeMethod(handle, Function.identity(), List.of(parameter));
		assertEquals(42, method.invoke(new Object[]{42}));
	}

	@DisplayName("A native parameter can be returned by-reference")
	@Test
	void returnedParameter() {
		final MethodHandle handle = MethodHandles.identity(int.class);
		final NativeMethod method = new NativeMethod(handle, Function.identity(), List.of(new NativeParameter(integer, true)));
//		assertEquals(42, method.invoke(new Object[]{42}));
		// TODO
	}

	@DisplayName("The signature of a native method must match the parameter specification")
	@Test
	void count() {
		final MethodHandle handle = MethodHandles.identity(int.class);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, Function.identity(), List.of()));
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, Function.identity(), List.of(parameter, parameter)));
	}

	@Nested
	class BuilderTests {
		private Registry registry;
		private Builder builder;

		@BeforeEach
		void before() {
			registry = new Registry();
			builder = new Builder(registry);
		}

		@DisplayName("A native method can be constructed programatically")
		@Test
		void build() {
			final MemorySegment abs = Linker
					.nativeLinker()
					.defaultLookup()
					.find("abs")
					.orElseThrow();

			registry.add(int.class, integer);

			final NativeMethod method = builder
					.address(abs)
					.returns(int.class)
					.parameter(int.class)
					.build();

			assertEquals(3, method.invoke(new Object[]{-3}));
		}

		@DisplayName("The return type and parameters of a native method must be registered")
		@Test
		void unsupported() throws Exception {
			assertThrows(IllegalArgumentException.class, () -> builder.returns(Object.class));
			assertThrows(IllegalArgumentException.class, () -> builder.parameter(Object.class));
		}
	}
}
