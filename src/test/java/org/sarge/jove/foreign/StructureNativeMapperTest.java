package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureNativeMapper;

class StructureNativeMapperTest {
	protected static class MockStructure extends NativeStructure {
		public int field;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
		}
	}

	private StructureNativeMapper mapper;
	private MockStructure structure;

	@BeforeEach
	void before() {
		final var registry = new NativeMapperRegistry();
		registry.add(new PrimitiveNativeMapper<>(int.class));
		mapper = new StructureNativeMapper().derive(MockStructure.class, registry);
		structure = new MockStructure();
	}

	@Test
	void mapper() {
		assertEquals(NativeStructure.class, mapper.type());
		assertEquals(structure.layout(), mapper.layout());
	}

	@Test
	void marshal() {
		structure.field = 2;
		final MemorySegment address = mapper.marshal(structure, Arena.ofAuto());
		assertEquals(2, address.get(JAVA_INT, 0));
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, mapper.empty());
	}

	private static MemorySegment address() {
		final var structure = new MockStructure();
		final MemorySegment address = Arena.ofAuto().allocate(structure.layout());
		address.set(JAVA_INT, 0, 3);
		return address;
	}

	@Test
	void returns() {
		final MemorySegment address = address();
		final var result = (MockStructure) mapper.returns().apply(address);
		assertEquals(3, result.field);
	}

	@Test
	void unmarshal() {
		final var structure = new MockStructure();
		final MemorySegment address = address();
		mapper.reference().accept(address, structure);
		assertEquals(3, structure.field);
	}
}
