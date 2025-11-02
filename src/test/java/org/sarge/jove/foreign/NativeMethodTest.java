package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.List;
import java.util.function.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeMethod.NativeParameter;

class NativeMethodTest {
	private IdentityTransformer<Integer> identity;
	private NativeParameter parameter;

	@BeforeEach
	void before() {
		identity = new IdentityTransformer<>(ValueLayout.JAVA_INT);
		parameter = new NativeParameter(identity, false);
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
    		final var method = new NativeMethod(handle, identity, List.of(parameter));
    		assertEquals(42, method.invoke(new Object[]{42}));
    	}

    	@DisplayName("can marshal NULL arguments")
    	@Test
    	void empty() {
    		final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class, MemorySegment.class));
    		final var method = new NativeMethod(handle, null, List.of(new NativeParameter(new StringTransformer(), false)));
    		method.invoke(new Object[]{null});
    	}

    	@DisplayName("must have the same number of transformers")
    	@Test
    	void mismatchedParameterTransformers() {
    		final MethodHandle handle = MethodHandles.identity(int.class);
    		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, identity, List.of()));
    		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(handle, identity, List.of(parameter, parameter)));
    	}
	}

	private static class MockReturnedTransformer implements Transformer<Object, MemorySegment> {
		private Object arg;
		private boolean returned;

		@Override
		public MemoryLayout layout() {
			return MockStructure.LAYOUT;
		}

		@Override
		public MemorySegment marshal(Object arg, SegmentAllocator allocator) {
			assertNotNull(arg);
			this.arg = arg;
			return MemorySegment.ofAddress(42);
		}

		@Override
		public Function<MemorySegment, Object> unmarshal() {
			return null;
		}

		@Override
		public BiConsumer<MemorySegment, Object> update() {
			return (_, arg) -> {
				assertEquals(this.arg, arg);
				returned = true;
			};
		}
	}

	@Nested
	class NativeParameterTest {
		private MockReturnedTransformer transformer;

		@BeforeEach
		void before() {
    		transformer = new MockReturnedTransformer();
		}

		@Test
    	void value() {
    		final var parameter = new NativeParameter(transformer, false);
    		assertEquals(false, parameter.isReturned());
    		assertEquals(MockStructure.LAYOUT, parameter.layout());
    	}

    	@Test
    	void returned() {
    		final var parameter = new NativeParameter(transformer, true);
    		assertEquals(true, parameter.isReturned());
    		assertEquals(ValueLayout.ADDRESS, parameter.layout());
    	}
	}

	@DisplayName("A native method with a by-reference parameter...")
	@Nested
	class ReturnedReferenceParameter {
		private NativeMethod method;
		private MockReturnedTransformer transformer;

		@BeforeEach
		void before() throws Exception {
			final MethodHandle handle = MethodHandles.empty(MethodType.methodType(void.class, MemorySegment.class));
    		transformer = new MockReturnedTransformer();
    		method = new NativeMethod(handle, null, List.of(new NativeParameter(transformer, true)));
		}

		@DisplayName("marshals by-reference parameters")
    	@Test
    	void marshal() {
			final var arg = new MockStructure();
    		method.invoke(new Object[]{arg});
    		assertEquals(arg, transformer.arg);
    	}

		@DisplayName("can marshal a NULL by-reference parameter")
    	@Test
    	void empty() {
    		method.invoke(new Object[]{null});
    		assertEquals(false, transformer.returned);
    		assertEquals(null, transformer.arg);
		}

		@DisplayName("updates by-reference parameters after invocation")
    	@Test
    	void update() {
    		method.invoke(new Object[]{new MockStructure()});
    		assertEquals(true, transformer.returned);
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
