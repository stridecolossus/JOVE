package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeBooleanTransformerTest {
	private NativeBooleanTransformer transformer;

	@BeforeEach
	void before() {
		transformer = new NativeBooleanTransformer();
	}

	@Test
	void marshal() {
		assertEquals(1, transformer.marshal(true, null));
		assertEquals(0, transformer.marshal(false, null));
	}

	@Test
	void empty() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.empty());
	}

	@Test
	void unmarshal() {
		assertEquals(true, transformer.unmarshal().apply(1));
		assertEquals(false, transformer.unmarshal().apply(0));
	}

	@Disabled("TODO")
	@Test
	void array() {
		final var allocator = Arena.ofAuto();
		final Transformer array = transformer.array();
		final var result = (MemorySegment) array.marshal(new boolean[]{true, false}, allocator);
		assertEquals(1, result.getAtIndex(ValueLayout.JAVA_INT, 0L));
		assertEquals(0, result.getAtIndex(ValueLayout.JAVA_INT, 1L));
	}

	@Test
	void isTrue() {
		assertEquals(false, NativeBooleanTransformer.isTrue(0));
		assertEquals(true, NativeBooleanTransformer.isTrue(1));
		assertEquals(true, NativeBooleanTransformer.isTrue(-1));
	}
}
