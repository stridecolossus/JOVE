package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.NativeStructure.StructureNativeMapper;

class StructureNativeMapperTest {
	protected static class MockStructure extends NativeStructure {
		public int field = 2;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
		}
	}

	private StructureNativeMapper mapper;

	@BeforeEach
	void before() {
		mapper = new StructureNativeMapper(NativeMapperRegistry.create());
	}

	@Test
	void mapper() {
		assertEquals(NativeStructure.class, mapper.type());
		assertEquals(ValueLayout.ADDRESS, mapper.layout());
	}

	@Test
	void toNative() {
		final MemorySegment address = mapper.toNative(new MockStructure(), new NativeContext());
		assertEquals(2, address.get(JAVA_INT, 0));
	}

	@Test
	void toNativeNull() {
		assertEquals(MemorySegment.NULL, mapper.toNativeNull(null));
	}

	@Test
	void fromNative() {
		final MemorySegment address = mapper.toNative(new MockStructure(), new NativeContext());
		final var result = (MockStructure) mapper.fromNative(address, MockStructure.class);
		assertEquals(2, result.field);
	}
}
