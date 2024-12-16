package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;

class PrimitiveNativeTransformerTest {
	private PrimitiveNativeTransformer<Integer> transformer;

	@BeforeEach
	void before() {
		transformer = PrimitiveNativeTransformer.of(int.class);
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> PrimitiveNativeTransformer.of(Object.class));
	}

	@Test
	void constructor() {
		assertEquals(ValueLayout.JAVA_INT, transformer.layout());
	}

	@DisplayName("A primitive integer is mapped to a native integer without conversion")
	@Test
	void transform() {
		assertEquals(42, transformer.transform(42, null, null));
	}

	@DisplayName("A primitive integer cannot be null")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> transformer.transform(null, null, null));
	}

	@DisplayName("A primitive integer can be returned from a native method without conversion")
	@Test
	void returns() {
		assertEquals(42, transformer.returns().apply(42));
	}

// TODO
//	@Test
//	void primitives() {
//		final var primitives = Map.of(
//            	byte.class,		ValueLayout.JAVA_BYTE,
//            	char.class,		ValueLayout.JAVA_CHAR,
//            	boolean.class,	ValueLayout.JAVA_BOOLEAN,
//            	int.class,		ValueLayout.JAVA_INT,
//            	short.class,	ValueLayout.JAVA_SHORT,
//            	long.class,		ValueLayout.JAVA_LONG,
//            	float.class,	ValueLayout.JAVA_FLOAT,
//            	double.class,	ValueLayout.JAVA_DOUBLE
//		);
//
//		assertEquals(primitives.size(), PrimitiveNativeTransformer.primitives().size());
//
//		for(PrimitiveNativeTransformer<?> transformer : PrimitiveNativeTransformer.primitives()) {
//			final Class type = transformer.type();
//			final ValueLayout expected = primitives.get(transformer.type());
//			assertEquals(expected, transformer.layout(type));
//		}
//	}
}
