package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.*;

class NativeMapperRegistryTest {
	private NativeMapperRegistry registry;

	@BeforeEach
	void before() {
		registry = new NativeMapperRegistry();
	}

	@Test
	void unsupported() {
		assertEquals(true, registry.mapper(Integer.class).isEmpty());
	}

	@Test
	void add() {
		final var mapper = new PrimitiveNativeMapper<>(int.class);
		registry.add(mapper);
		assertEquals(Optional.of(mapper), registry.mapper(int.class));
	}

//	@Nested
//	class DerivedTypeTests {
//		private NativeMapper<?> mapper;
//
//		@BeforeEach
//    	void before() {
//    		mapper = new DefaultNativeMapper<>(Number.class, ValueLayout.JAVA_INT);
//    		registry.add(mapper);
//    	}
//
//    	@Test
//    	void derived() {
//    		assertEquals(Optional.of(mapper), registry.mapper(Number.class));
//    	}
//
//    	@Test
//    	void wrapper() {
//    		assertEquals(Optional.of(mapper), registry.mapper(Integer.class));
//    	}
//	}
}
