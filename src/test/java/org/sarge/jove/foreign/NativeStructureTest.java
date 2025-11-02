package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeStructureTest {
	private MockStructure structure;
	private Registry registry;
	private StructureTransformerFactory factory;
	private StructureTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		structure = new MockStructure();
		registry = new Registry();
		registry.register(int.class, new IdentityTransformer<>(JAVA_INT));
		factory = new StructureTransformerFactory(registry);
		transformer = factory.transformer(MockStructure.class);
		allocator = Arena.ofAuto();
	}

	@Test
	void layout() {
		assertEquals(structure.layout(), transformer.layout());
	}

	@DisplayName("A native structure can be marshalled to off-heap memory")
	@Test
	void marshal() {
		// Populate structure
		structure.field = 3;

		// Marshal structure and check off-heap memory
		final MemorySegment address = transformer.marshal(structure, allocator);
		assertEquals(4 + 4, address.byteSize());
		assertEquals(3, address.get(JAVA_INT, 0L));
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@DisplayName("A native structure can be marshalled from off-heap memory")
	@Test
	void unmarshal() {
		// Init off-heap representation
		final MemorySegment address = allocator.allocate(structure.layout());
		address.set(JAVA_INT, 0L, 3);

		// Unmarshal structure instance from the off-heap memory
		final var result = (MockStructure) transformer.unmarshal().apply(address);
		assertEquals(structure.layout(), result.layout());
		assertEquals(3, result.field);
	}

	@Test
	void update() {
		final MemorySegment address = allocator.allocate(structure.layout());
		address.set(JAVA_INT, 0L, 3);
		transformer.update().accept(address, structure);
		assertEquals(3, structure.field);
	}

	@DisplayName("A native structure field cannot be anonymous")
    @Test
    void anonymous() {
		class AnonymousField implements NativeStructure {
	    	@Override
	    	public StructLayout layout() {
	    		return MemoryLayout.structLayout(
	    				JAVA_INT,
	    				MemoryLayout.paddingLayout(4)
	    		);
	    	}
		}

		assertThrows(IllegalArgumentException.class, () -> factory.transformer(AnonymousField.class));
    }

	@DisplayName("The name of a native structure field must correspond to a member of the structure")
	@Test
	void unknown() {
		class UnknownField implements NativeStructure {
	    	@Override
	    	public StructLayout layout() {
	    		return MemoryLayout.structLayout(
	    				JAVA_INT.withName("cobblers"),
	    				MemoryLayout.paddingLayout(4)
	    		);
	    	}
		}

		assertThrows(IllegalArgumentException.class, () -> factory.transformer(UnknownField.class));
	}

	@DisplayName("A native structure field must have a supported type")
	@Test
	void unsupported() {
		class UnsupportedField implements NativeStructure {
			@SuppressWarnings("unused")
			private boolean field;

	    	@Override
	    	public StructLayout layout() {
	    		return MemoryLayout.structLayout(
	    				ValueLayout.JAVA_BOOLEAN.withName("field"),
	    				MemoryLayout.paddingLayout(4)
	    		);
	    	}
		}

		assertThrows(IllegalArgumentException.class, () -> factory.transformer(UnsupportedField.class));
	}
}
