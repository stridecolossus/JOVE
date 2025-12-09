package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.reflect.Field;

import org.junit.jupiter.api.*;

@SuppressWarnings("rawtypes")
class FieldMappingTest {
	private static class MockFieldMarshal implements FieldMarshal {
		@Override
		public void marshal(Object value, Transformer transformer, MemorySegment address, SegmentAllocator allocator) {
			address.set(JAVA_INT, 0L, (int) value);
		}

		@Override
		public Object unmarshal(MemorySegment address, Transformer transformer) {
			return address.get(JAVA_INT, 0L);
		}
	}

	private FieldMapping mapping;
	private VarHandle handle;
	private Transformer transformer;
	private MockFieldMarshal marshal;
	private SegmentAllocator allocator;
	private MockStructure structure;

	@BeforeEach
	void before() throws Exception {
		final Field field = MockStructure.class.getField("field");
		handle = MethodHandles.lookup().unreflectVarHandle(field);
		transformer = new PrimitiveTransformer<>(JAVA_INT);
		marshal = new MockFieldMarshal();
		mapping = new FieldMapping(handle, transformer, marshal);
		structure = new MockStructure();
		allocator = Arena.ofAuto();
	}

	@Test
	void marshal() {
		final MemorySegment address = allocator.allocate(MockStructure.LAYOUT);
		structure.field = 2;
		mapping.marshal(structure, address, allocator);
		assertEquals(2, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		final MemorySegment address = allocator.allocate(MockStructure.LAYOUT);
		address.set(JAVA_INT, 0L, 3);
		mapping.unmarshal(address, structure);
		assertEquals(3, structure.field);
	}
}
