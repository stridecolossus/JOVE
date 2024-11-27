package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import org.junit.jupiter.api.*;

class AtomicFieldMappingTest {
	private static class MockStructure extends NativeStructure {
		@SuppressWarnings("unused")
		public int field;

		@Override
		protected StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT);
		}
	}

	private AtomicFieldMapping mapping;
	private MemorySegment address;
	private MockStructure structure;

	@BeforeEach
	void before() throws NoSuchFieldException, IllegalAccessException {
		structure = new MockStructure();
		address = Arena.ofAuto().allocate(structure.layout());

		final VarHandle handle = structure.layout().varHandle(PathElement.groupElement(0));
		mapping = new AtomicFieldMapping(handle);
	}

	@Test
	void marshal() {
		mapping.marshal(42, address, null);
		assertEquals(42, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		address.set(JAVA_INT, 0L, 42);
		assertEquals(42, mapping.unmarshal(address, structure));
	}
}
