package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;
import org.sarge.jove.util.IntEnum.ReverseMapping;

class IntEnumNativeTransformerTest {
	private static enum MockEnum implements IntEnum {
		INSTANCE;

		@Override
		public int value() {
			return 42;
		}
	}

	private IntEnumNativeTransformer transformer;

	@BeforeEach
	void before() {
		transformer = new IntEnumNativeTransformer(ReverseMapping.get(MockEnum.class));
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.JAVA_INT, transformer.layout());
	}

	@DisplayName("An integer enumeration value is transformed to a native integer")
	@Test
	void transform() {
		assertEquals(42, transformer.transform(MockEnum.INSTANCE, ParameterMode.VALUE, null));
	}

	@DisplayName("An empty integer enumeration value is the default value of the enumeration")
	@Test
	void empty() {
		assertEquals(42, transformer.transform(null, ParameterMode.VALUE, null));
	}

	@DisplayName("An integer enumeration can be returned from a native method")
	@Test
	void returned() {
		assertEquals(MockEnum.INSTANCE, transformer.returns().apply(42));
	}

	@DisplayName("An integer enumeration can be returned from a native method as the default value")
	@Test
	void returnedDefault() {
		assertEquals(MockEnum.INSTANCE, transformer.returns().apply(0));
	}

	@DisplayName("An integer enumeration cannot be passed as a by-reference parameter")
	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}
}
