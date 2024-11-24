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
	private NativeContext context;

	@BeforeEach
	void before() {
		final var registry = new NativeMapperRegistry();
		registry.add(new PrimitiveNativeMapper<>(int.class));
		mapper = new StructureNativeMapper(registry);
		context = new NativeContext(Arena.ofAuto(), registry);
		structure = new MockStructure();
	}

	@Test
	void mapper() {
		assertEquals(NativeStructure.class, mapper.type());
		assertEquals(structure.layout(), mapper.layout(MockStructure.class));
	}

	@Test
	void marshal() {
		structure.field = 2;
		final MemorySegment address = mapper.marshal(structure, context);
		assertEquals(2, address.get(JAVA_INT, 0));
	}

	@Test
	void marshalNull() {
		assertEquals(MemorySegment.NULL, mapper.marshalNull(MockStructure.class));
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
		final var result = (MockStructure) mapper.returns(MockStructure.class).apply(address);
		assertEquals(3, result.field);
	}

	@Test
	void unmarshal() {
		final var structure = new MockStructure();
		final MemorySegment address = address();
		mapper.unmarshal(MockStructure.class).accept(address, structure);
		assertEquals(3, structure.field);
	}
}
