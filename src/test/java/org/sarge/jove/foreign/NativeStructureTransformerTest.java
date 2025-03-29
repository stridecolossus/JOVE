package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeTransformer.Factory;

class NativeStructureTransformerTest {
	private static class MockStructure implements NativeStructure {
		public int field;

		public MockStructure() {
		}

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
		}
	}

	private Factory<NativeStructure> factory;
	private NativeRegistry registry;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		registry = new NativeRegistry();
		registry.add(int.class, new IdentityNativeTransformer<>(JAVA_INT));
		factory = NativeStructureTransformer.factory(registry);
	}

	@Test
	void layout() {
		// TODO - only if ptr to structure!!!
		assertEquals(ValueLayout.ADDRESS, factory.create(MockStructure.class).layout());
	}

	@Test
	void marshal() {
		final var structure = new MockStructure();
		structure.field = 42;

		final MemorySegment address = (MemorySegment) factory.create(MockStructure.class).marshal(structure, arena);
		assertEquals(4, address.byteSize());
		assertEquals(42, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		final var instance = new MockStructure();
		final MemorySegment address = arena.allocate(instance.layout());
		address.set(JAVA_INT, 0, 42);

		final var transformer = (NativeStructureTransformer) factory.create(MockStructure.class);
		final MockStructure structure = (MockStructure) transformer.unmarshal().apply(address);
		assertEquals(42, structure.field);
	}

	@SuppressWarnings("unused")
	private static class Anonymous implements NativeStructure {
		public Anonymous() {
		}

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT);
		}
	}

	@Test
	void named() {
		assertThrows(IllegalArgumentException.class, () -> factory.create(Anonymous.class));
	}

	@SuppressWarnings("unused")
	private static class Invalid implements NativeStructure {
		private int field;

		public Invalid() {
		}

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
		}
	}

	@Test
	void access() {
		final var factory = NativeStructureTransformer.factory(registry);
		assertThrows(RuntimeException.class, () -> factory.create(Invalid.class));
	}

	@Test
	void unsupported() {
		final var factory = NativeStructureTransformer.factory(new NativeRegistry());
		assertThrows(IllegalArgumentException.class, () -> factory.create(MockStructure.class));
	}
}
