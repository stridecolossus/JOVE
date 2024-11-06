package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.IntEnum;

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
	void type() {
		assertEquals(IntEnum.class, mapper.type());
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.JAVA_INT, mapper.layout());
	}

	@Test
	void toNative() {
		assertEquals(42, mapper.toNative(MockEnum.INSTANCE, IntEnum.class, null));
	}

	@Test
	void toNativeNull() {
		assertEquals(42, mapper.toNative(MockEnum.INSTANCE, IntEnum.class, null));
	}

	@Test
	void fromNative() {
		assertEquals(MockEnum.INSTANCE, mapper.fromNative(42, MockEnum.class));
	}

	@Test
	void fromNativeNull() {
		assertEquals(MockEnum.INSTANCE, mapper.fromNative(null, MockEnum.class));
	}
}
