package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeContext;

class IntEnumNativeMapperTest {
	private static enum MockEnum implements IntEnum {
		INSTANCE;

		@Override
		public int value() {
			return 42;
		}
	}

	private IntEnumNativeMapper mapper;

	@BeforeEach
	void before() {
		mapper = new IntEnumNativeMapper();
	}

	@Test
	void mapper() {
		assertEquals(IntEnum.class, mapper.type());
		assertEquals(ValueLayout.JAVA_INT, mapper.layout(null));
	}

	@Test
	void marshal() {
		assertEquals(42, mapper.marshal(MockEnum.INSTANCE, new NativeContext()));
	}

	@Test
	void marshalNull() {
		assertEquals(42, mapper.marshalNull(MockEnum.class));
	}

	@Test
	void unmarshal() {
		assertEquals(MockEnum.INSTANCE, mapper.unmarshal(42, MockEnum.class));
	}

	@Test
	void unmarshalDefault() {
		assertEquals(MockEnum.INSTANCE, mapper.unmarshal(0, MockEnum.class));
	}
}
