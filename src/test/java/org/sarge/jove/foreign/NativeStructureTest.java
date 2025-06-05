package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureTransformerFactory;

class NativeStructureTest {
	private MockStructure structure;

	@BeforeEach
	void before() {
		structure = new MockStructure();
	}

	@Nested
	class TransformerTest {
		private Registry registry;
		private StructureTransformerFactory factory;
		private Transformer<NativeStructure> transformer;
		private SegmentAllocator allocator;

		@BeforeEach
		void before() {
			registry = new Registry();
			registry.add(int.class, new IdentityTransformer<>(JAVA_INT));
			factory = new StructureTransformerFactory(registry);
			transformer = factory.create(MockStructure.class);
			allocator = Arena.ofAuto();
		}

		@Test
		void layout() {
			assertEquals(structure.layout(), transformer.layout());
		}

		@Test
		void marshal() {
			// Init structure
			structure.field = 42;

			// Marshal to off-heap memory
			final MemorySegment address = (MemorySegment) transformer.marshal(structure, allocator);
			assertEquals(42, address.get(JAVA_INT, 0));
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Test
		void unmarshal() {
			// Init off-heap memory
			final MemorySegment address = allocator.allocate(structure.layout());
			address.set(JAVA_INT, 0, 42);

			// Unmarshal to new instance
			final Function unmarshal = transformer.unmarshal();
			final var result = (MockStructure) unmarshal.apply(address);
			assertEquals(42, result.field);
		}

    	@Test
    	void array() {
    		// Init structure array
    		structure.field = 42;
    		final NativeStructure[] array = {structure, structure};

    		// Wrap as an array transformer
    		final var parent = new ArrayTransformer<>(transformer);
    		assertEquals(ValueLayout.ADDRESS, parent.layout());

    		// Check marshalled as a contiguous memory block
    		final MemorySegment address = parent.marshal(array, allocator);
    		assertEquals(2 * 8, address.byteSize());
    		assertEquals(42, address.get(JAVA_INT, 0));
    		assertEquals(42, address.get(JAVA_INT, 8));
    	}
    }
}
