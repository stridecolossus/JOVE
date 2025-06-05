package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.FieldMapping.*;

class FieldMappingTest {
	private MockStructure structure;
	private Transformer<Integer> transformer;
	private Registry registry;

	@BeforeEach
	void before() {
		structure = new MockStructure();
		transformer = new IdentityTransformer<>(ValueLayout.JAVA_INT);
		registry = new Registry();
		registry.add(int.class, transformer);
	}

	@Nested
	class BuilderTest {
		private Builder builder;
		private SegmentAllocator allocator;
		private MemorySegment address;

		@BeforeEach
		void before() {
			builder = new Builder(registry);
			allocator = Arena.ofAuto();
			address = allocator.allocate(structure.layout());
		}

    	@Test
    	void marshal() {
    		// Build field mappings
    		final CompoundFieldMapping mapping = builder.build(MockStructure.class);

    		// Marshal to off-heap memory
    		structure.field = 42;
    		mapping.marshal(structure, address, allocator);
    		assertEquals(42, address.get(ValueLayout.JAVA_INT, 0));

    		// Unmarshal back to a new instance
    		final MockStructure result = (MockStructure) mapping.unmarshal(address);
    		assertEquals(42, result.field);
    	}

    	// TODO - nested

		public static class AnonymousField implements NativeStructure {
			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(ValueLayout.JAVA_INT);
			}
		}

		@Test
    	void anonymous() {
    		assertThrows(IllegalArgumentException.class, () -> builder.build(AnonymousField.class));
    	}

		public static class UnknownField implements NativeStructure {
			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("cobblers"));
			}
		}

		@Test
    	void unknown() {
    		assertThrows(IllegalArgumentException.class, () -> builder.build(UnknownField.class));
    	}

		public static class UnsupportedLayout implements NativeStructure {
			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(MemoryLayout.sequenceLayout(1, PADDING));
			}
		}

		@Test
    	void unsupported() {
    		assertThrows(IllegalArgumentException.class, () -> builder.build(UnsupportedLayout.class));
    	}
    }
}
