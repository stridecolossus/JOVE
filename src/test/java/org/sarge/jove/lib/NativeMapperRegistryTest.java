package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
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
		final var mapper = new IntegerNativeMapper();
		registry.add(mapper);
		assertEquals(Optional.of(mapper), registry.mapper(int.class));
	}

	@Test
	void defaults() {

	}

	@Nested
	class DerivedTypeTests {
		private static class MockNativeMapper implements NativeMapper<Number> {
			@Override
			public Class<Number> type() {
				return Number.class;
			}

			@Override
			public ValueLayout layout() {
				return null;
			}

			@Override
			public Object toNative(Object value, Arena arena) {
				return null;
			}

			@Override
			public Number fromNative(Object value) {
				return null;
			}
		}

		private NativeMapper<Number> mapper;

		@BeforeEach
    	void before() {
    		mapper = new MockNativeMapper();
    		registry.add(mapper);
    	}

    	@Test
    	void derived() {
    		assertEquals(Optional.of(mapper), registry.mapper(Number.class));
    	}

    	@Test
    	void wrapper() {
    		assertEquals(Optional.of(mapper), registry.mapper(Integer.class));
    	}
	}
}
