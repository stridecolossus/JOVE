package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeMethod.*;

class NativeMethodTest {
	private IdentityTransformer integer;

	@BeforeEach
	void before() {
		integer = new IdentityTransformer(JAVA_INT);
	}

	@Test
	void invoke() {
		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class));
		final NativeMethod method = new NativeMethod(handle, null, List.of());
		assertEquals(null, method.invoke(null));
	}

	@Test
	void returns() {
		final MethodHandle handle = MethodHandles.constant(int.class, 42);
		final NativeMethod method = new NativeMethod(handle, integer, List.of());
		assertEquals(42, method.invoke(null));
	}

	@Test
	void returnsRequiresTransformer() {
		final MethodHandle handle = MethodHandles.zero(int.class);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, null, List.of()));
	}

	@Test
	void returnsVoidTransformer() {
		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class));
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, integer, List.of()));
	}

	@Test
	void parameter() {
		final MethodHandle handle = MethodHandles.identity(int.class);
		final NativeMethod method = new NativeMethod(handle, integer, List.of(new Parameter(integer, false)));
		assertEquals(42, method.invoke(new Object[]{42}));
	}

	@Test
	void count() {
		final MethodHandle handle = MethodHandles.identity(int.class);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, null, List.of()));
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, null, List.of(new Parameter(integer, false), new Parameter(integer, false))));
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

		@Test
		void descriptor() {
			assertEquals(FunctionDescriptor.ofVoid(), Builder.descriptor(null, List.of()));
		}

		@Test
		void returns() {
			assertEquals(FunctionDescriptor.of(JAVA_INT), Builder.descriptor(integer, List.of()));
		}

		@Test
		void parameter() {
			assertEquals(FunctionDescriptor.ofVoid(JAVA_INT), Builder.descriptor(null, List.of(new Parameter(integer, false))));
		}

		@Test
		void unsupported() throws Exception {
			assertThrows(IllegalArgumentException.class, () -> builder.returns(Object.class));
			assertThrows(IllegalArgumentException.class, () -> builder.parameter(Object.class));
		}
	}
}
