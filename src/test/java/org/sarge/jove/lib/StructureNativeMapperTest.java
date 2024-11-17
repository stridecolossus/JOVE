package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.NativeStructure.StructureNativeMapper;

class StructureNativeMapperTest {
	private StructureNativeMapper mapper;

	@BeforeEach
	void before() {
		mapper = new StructureNativeMapper();
	}

	@Test
	void mapper() {
		assertEquals(NativeStructure.class, mapper.type());
		assertEquals(ValueLayout.ADDRESS, mapper.layout());
	}

	@Test
	void toNative() {
		final var structure = new NativeStructure() {
			@SuppressWarnings("unused")
			public int field = 2;

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT.withName("field"));
			}
		};
		final MemorySegment address = mapper.toNative(structure, new NativeContext());
		assertEquals(2, address.get(JAVA_INT, 0));
	}

	@Test
	void toNativeNull() {
		assertEquals(MemorySegment.NULL, mapper.toNativeNull(null));
	}

	@Test
	void fromNative() {
		// TODO
	}
}
