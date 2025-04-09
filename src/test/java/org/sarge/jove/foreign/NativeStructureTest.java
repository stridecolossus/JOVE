package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureTransformer;

class NativeStructureTest {
	@SuppressWarnings("unused")
	@Nested
	class TransformerTests {
    	private StructureTransformer transformer;
    	private Registry registry;
    	private Arena arena;

    	@BeforeEach
    	void before() {
    		arena = Arena.ofAuto();
    		registry = MockStructure.registry();
    		transformer = new StructureTransformer.Builder(registry).build(MockStructure.class);
    	}

    	@Test
    	void layout() {
    		final var structure = new MockStructure();
    		assertEquals(structure.layout(), transformer.layout());
    	}

    	@Test
    	void marshal() {
    		final var structure = new MockStructure();
    		structure.field = 42;

    		final MemorySegment address = transformer.marshal(structure, arena);
    		assertEquals(4, address.byteSize());
    		assertEquals(42, address.get(JAVA_INT, 0L));
    	}

    	@Test
    	void unmarshal() {
    		final var instance = new MockStructure();
    		final MemorySegment address = arena.allocate(instance.layout());
    		address.set(JAVA_INT, 0, 42);

    		final MockStructure structure = (MockStructure) transformer.unmarshal(address);
    		assertEquals(42, structure.field);
    	}

    	// TODO - by-reference? arrays?

    	// TODO - nested structure

		private static class Anonymous extends MockStructure {
			public Anonymous() {
			}

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT);
			}
		}

		@Test
    	void anonymous() {
			assertThrows(IllegalArgumentException.class, () -> new StructureTransformer.Builder(registry).build(Anonymous.class));
    	}

		private static class UnsupportedMemberLayout implements NativeStructure {
			public int field;

			public UnsupportedMemberLayout() {
			}

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(MemoryLayout.sequenceLayout(1, JAVA_INT).withName("field"));
			}
		}

		@Test
    	void member() {
			assertThrows(IllegalArgumentException.class, () -> new StructureTransformer.Builder(registry).build(UnsupportedMemberLayout.class));
    	}

		private static class MissingDefaultConstructor implements NativeStructure {
			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT.withName("field"));
			}
		}

		@Test
    	void constructor() {
			assertThrows(RuntimeException.class, () -> new StructureTransformer.Builder(registry).build(MissingDefaultConstructor.class));
    	}

		private static class InaccessibleField implements NativeStructure {
			private int field;

			public InaccessibleField() {
			}

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT.withName("field"));
			}
		}

		@Test
    	void access() {
			assertThrows(RuntimeException.class, () -> new StructureTransformer.Builder(registry).build(InaccessibleField.class));
    	}
    }
}
