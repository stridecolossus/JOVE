package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.List;

import org.junit.jupiter.api.*;

class NativeMethodTest {
	private PrimitiveTransformer<Integer> identity;

	@BeforeEach
	void before() {
		identity = new PrimitiveTransformer<>(ValueLayout.JAVA_INT);
	}

	@DisplayName("A simple native method without a return type or parameters can be invoked")
	@Test
	void invoke() {
		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class));
		final var method = new NativeMethod(handle, null, List.of());
		assertEquals(null, method.invoke(null));
	}

	@DisplayName("A native method with a return type...")
	@Nested
	class ReturnValue {
    	@DisplayName("unmarshals the return value")
    	@Test
    	void unmarshal() {
    		final MethodHandle handle = MethodHandles.constant(int.class, 42);
    		final var method = new NativeMethod(handle, identity, List.of());
    		assertEquals(42, method.invoke(null));
    	}

    	@DisplayName("can unmarshal a NULL return value")
    	@Test
    	void empty() {
    		final MethodHandle handle = MethodHandles.constant(MemorySegment.class, MemorySegment.NULL);
    		final var method = new NativeMethod(handle, new StringTransformer(), List.of());
    		assertEquals(null, method.invoke(null));
    	}

    	@DisplayName("must configure a return transformer")
    	@Test
    	void missingReturnTransformer() {
    		final MethodHandle handle = MethodHandles.constant(int.class, 42);
    		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, null, List.of()));
    	}
	}

	@DisplayName("A native method with one-or-more parameters...")
	@Nested
	class Parameters {
    	@DisplayName("marshals the given arguments")
    	@Test
    	void parameter() {
    		final MethodHandle handle = MethodHandles.identity(int.class);
    		final var method = new NativeMethod(handle, identity, List.of(identity));
    		assertEquals(42, method.invoke(new Object[]{42}));
    	}

    	@DisplayName("can marshal NULL arguments")
    	@Test
    	void empty() {
    		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class, MemorySegment.class));
    		final var method = new NativeMethod(handle, null, List.of(new StringTransformer()));
    		method.invoke(new Object[]{null});
    	}

    	@DisplayName("must have the same number of transformers")
    	@Test
    	void mismatchedParameterTransformers() {
    		final MethodHandle handle = MethodHandles.identity(int.class);
    		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, identity, List.of()));
    		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, identity, List.of(identity, identity)));
    	}
	}

	@DisplayName("A native method with a by-reference parameter...")
	@Nested
	class ReturnedReferenceParameter {
		private NativeMethod method;
		private UpdateTransformer<MockStructure> transformer;

		@BeforeEach
		void before() throws Exception {
			final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class, MemorySegment.class));
			transformer = new UpdateTransformer<>(new MockStructureTransformer());
    		method = new NativeMethod(handle, null, List.of(transformer));
		}

		@DisplayName("marshals by-reference parameters")
    	@Test
    	void marshal() {
			final var structure = new MockStructure();
    		method.invoke(new Object[]{structure});
    	}

		@DisplayName("can marshal a NULL by-reference parameter")
    	@Test
    	void empty() {
    		method.invoke(new Object[]{null});
		}

		@DisplayName("updates by-reference parameters after invocation")
    	@Test
    	void update() {
			final var structure = new MockStructure();
    		method.invoke(new Object[]{structure});
    		assertEquals(42, structure.field);
		}
	}

	@Test
	void equals() {
		final MethodHandle handle = MethodHandles.constant(int.class, 42);
		final var method = new NativeMethod(handle, identity, List.of());
		assertEquals(method, method);
		assertEquals(method, new NativeMethod(handle, identity, List.of()));
		assertNotEquals(method, null);
	}
}
