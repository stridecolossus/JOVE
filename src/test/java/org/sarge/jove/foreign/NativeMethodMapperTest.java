package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.List;

import org.junit.jupiter.api.*;

class NativeMethodMapperTest {

	private static interface MockMethod {
		int returns();
		String unknown();
		void none();
		void method(int parameter);
		void unknown(String parameter);
	}

	private NativeMethodMapper mapper;
	private PrimitiveTransformer<Integer> transformer;

	@BeforeEach
	void before() {
		final var registry = new Registry();
		transformer = new PrimitiveTransformer<>(ValueLayout.JAVA_INT);
		registry.add(int.class, transformer);
		mapper = new NativeMethodMapper(registry);
	}

	@Nested
	class ReturnTypeTest {
		@Test
		void map() throws Exception {
			final var method = MockMethod.class.getMethod("returns");
			assertEquals(transformer, mapper.returns(method));
		}

		@Test
		void none() throws Exception {
			final var method = MockMethod.class.getMethod("none");
			assertEquals(null, mapper.returns(method));
		}

		@Test
		void unknown() throws Exception {
			final var method = MockMethod.class.getMethod("unknown");
			assertThrows(IllegalArgumentException.class, () -> mapper.returns(method));
		}
	}

	@Nested
	class ParameterTest {
		@Test
		void map() throws Exception {
			final var method = MockMethod.class.getMethod("method", int.class);
			assertEquals(List.of(transformer), mapper.parameters(method));
		}

		@Test
		void none() throws Exception {
			final var method = MockMethod.class.getMethod("none");
			assertEquals(List.of(), mapper.parameters(method));
		}

		@Test
		void unknown() throws Exception {
			final var method = MockMethod.class.getMethod("unknown", String.class);
			assertThrows(IllegalArgumentException.class, () -> mapper.parameters(method));
		}
	}

	@Nested
	class FunctionDescriptorTest {
		@Test
		void descriptor() {
			final var expected = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
			final var descriptor = NativeMethodMapper.descriptor(transformer, List.of(transformer));
			assertEquals(expected, descriptor);
		}

		@Test
		void none() {
			final var expected = FunctionDescriptor.ofVoid(JAVA_INT);
			final var descriptor = NativeMethodMapper.descriptor(null, List.of(transformer));
			assertEquals(expected, descriptor);
		}
	}
}
