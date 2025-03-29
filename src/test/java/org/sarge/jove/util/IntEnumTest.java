package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.IntEnum.*;

class IntEnumTest {
	@Nested
	class ReverseMappingTests {
    	private ReverseMapping<MockEnum> mapping;

    	@BeforeEach
    	void before() {
    		mapping = IntEnum.reverse(MockEnum.class);
    	}

    	@DisplayName("A native value can be reverse mapped to an enumeration constant")
    	@Test
    	void map() {
    		assertEquals(MockEnum.A, mapping.map(1));
    		assertEquals(MockEnum.B, mapping.map(2));
    		assertEquals(MockEnum.C, mapping.map(4));
    	}

    	@DisplayName("An integer enumeration has a default value")
    	@Test
    	void defaultValue() {
    		assertEquals(MockEnum.A, mapping.defaultValue());
    	}

    	@DisplayName("An invalid native value cannot be reverse mapped")
    	@Test
    	void invalid() {
    		assertThrows(IllegalArgumentException.class, () -> mapping.map(0));
    		assertThrows(IllegalArgumentException.class, () -> mapping.map(999));
    	}
	}

	@Nested
	class TransformerTests {
		private IntEnumNativeTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new IntEnumNativeTransformer(MockEnum.class);
		}

		@Test
		void marshal() {
			assertEquals(1, transformer.marshal(MockEnum.A, null));
		}

		@Test
		void empty() {
			assertEquals(MockEnum.A, transformer.empty());
		}

		@Test
		void unmarshal() {
			assertEquals(MockEnum.A, transformer.unmarshal().apply(1));
		}
	}
}
