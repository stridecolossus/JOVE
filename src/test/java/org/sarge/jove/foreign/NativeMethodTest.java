package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.*;

class NativeMethodTest {
	private IdentityTransformer identity;

	@BeforeEach
	void before() {
		identity = new IdentityTransformer(ValueLayout.JAVA_INT);
	}

	@DisplayName("A simple native method without a return type or parameters can be invoked")
	@Test
	void invoke() {
		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class));
		final var method = new NativeMethod(handle, null, List.of());
		assertEquals(null, method.invoke(null));
	}

	@DisplayName("A native method can marshal the return value of the method")
	@Test
	void returns() {
		final MethodHandle handle = MethodHandles.constant(int.class, 42);
		final var method = new NativeMethod(handle, identity, List.of());
		assertEquals(42, method.invoke(null));
	}

	@DisplayName("A native method with a return value must configure a return transformer")
	@Test
	void missingReturnTransformer() {
		final MethodHandle handle = MethodHandles.constant(int.class, 42);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, null, List.of()));
	}

	@DisplayName("The transformer for the return value of a native method must be implemented")
	@Test
	void invalidReturnTransformer() {
		final MethodHandle handle = MethodHandles.constant(String.class, "whatever");
		final Transformer invalid = new StringTransformer() {
			@Override
			public Function<MemorySegment, String> unmarshal() {
				throw new UnsupportedOperationException();
			}
		};
		assertThrows(UnsupportedOperationException.class, () -> new NativeMethod(handle, invalid, List.of()));
	}

	@DisplayName("A native method can have none-or-more parameters")
	@Test
	void parameter() {
		final MethodHandle handle = MethodHandles.identity(int.class);
		final var method = new NativeMethod(handle, identity, List.of(identity));
		assertEquals(42, method.invoke(new Object[]{42}));
	}

	@DisplayName("The transformers for a native method must match the signature")
	@Test
	void mismatchedParameterTransformers() {
		final MethodHandle handle = MethodHandles.identity(int.class);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, identity, List.of()));
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, identity, List.of(identity, identity)));
	}

	@DisplayName("A native method can return by-reference parameters")
	@Test
	void reference() {
		// TODO
//		final MethodHandle handle = MethodHandles.constant(int.class, 2);
//		final var method = new NativeMethod(handle, null, List.of(transformer));
//		method.invoke(new Object[]{1});
	}
	// TODO - ignores null args[] or MemorySegment.NULL returned

	@Test
	void equals() {
		final MethodHandle handle = MethodHandles.constant(int.class, 42);
		final var method = new NativeMethod(handle, identity, List.of());
		assertEquals(method, method);
		assertEquals(method, new NativeMethod(handle, identity, List.of()));
		assertNotEquals(method, null);
	}
}
