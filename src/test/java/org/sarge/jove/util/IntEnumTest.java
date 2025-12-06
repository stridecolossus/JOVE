package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.Arena;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.Transformer;
import org.sarge.jove.util.IntEnum.*;

class IntEnumTest {
	@Nested
	class ReverseMappingTests {
    	private ReverseMapping<MockEnum> mapping;

    	@BeforeEach
    	void before() {
    		mapping = ReverseMapping.mapping(MockEnum.class);
    	}

    	@DisplayName("An enumeration constant can be mapped from a native value")
    	@Test
    	void map() {
    		assertEquals(MockEnum.A, mapping.map(1));
    		assertEquals(MockEnum.B, mapping.map(2));
    		assertEquals(MockEnum.C, mapping.map(4));
    	}

    	@Disabled // TODO - why are we getting these 'extra' values?
    	@DisplayName("An enumeration constant cannot be mapped from an invalid native value")
    	@Test
    	void invalid() {
    		assertThrows(IllegalArgumentException.class, () -> mapping.map(0));
    		assertThrows(IllegalArgumentException.class, () -> mapping.map(999));
    	}

    	@DisplayName("An integer enumeration has a default value")
    	@Test
    	void defaultValue() {
    		assertEquals(MockEnum.A, mapping.defaultValue());
    	}
	}

	@Nested
	class TransformerTests {
		private IntEnumTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new IntEnumTransformer(MockEnum.class);
		}

		@Test
		void marshal() {
			assertEquals(1, transformer.marshal(MockEnum.A, null));
		}

		@Test
		void empty() {
			assertEquals(1, transformer.empty());
		}

		@Test
		void unmarshal() {
			assertEquals(MockEnum.A, transformer.unmarshal().apply(1));
		}

		@Test
		void unmarshalDefaultValue() {
			assertEquals(MockEnum.A, transformer.unmarshal().apply(0));
		}

		@Test
		void update() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.update());
		}

		@SuppressWarnings({"rawtypes", "unchecked", "resource"})
		@Test
		void array() {
			final MockEnum[] array = {MockEnum.A, MockEnum.B};
			final Transformer delegate = transformer.array();
			final Object address = delegate.marshal(array, Arena.ofAuto());
			final var result = new MockEnum[2];
			delegate.update().accept(address, result);
			assertArrayEquals(array, result);
		}
	}
}
