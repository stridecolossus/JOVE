package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;

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
		mapper = new IntEnumNativeMapper().derive(MockEnum.class);
	}

	@Test
	void mapper() {
		assertEquals(IntEnum.class, mapper.type());
		assertEquals(ValueLayout.JAVA_INT, mapper.layout());
	}

	@Test
	void marshal() {
		assertEquals(42, mapper.marshal(MockEnum.INSTANCE, null));
	}

	@Test
	void marshalNull() {
		assertEquals(42, mapper.marshalNull());
	}

	@Test
	void returned() {
		assertEquals(MockEnum.INSTANCE, mapper.returns().apply(42));
	}

	@Test
	void returnedDefault() {
		assertEquals(MockEnum.INSTANCE, mapper.returns().apply(0));
	}

	@Test
	void reference() {
		assertThrows(UnsupportedOperationException.class, () -> mapper.reference());
	}
}
