package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.*;
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
		registry.add(int.class, new IdentityTransformer(JAVA_INT));
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
		structure.field = 3;

		final MemorySegment address = transformer.marshal(structure, allocator);
		assertEquals(4 + 4, address.byteSize());
		assertEquals(3, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		final MemorySegment address = allocator.allocate(structure.layout());
		address.set(JAVA_INT, 0L, 3);

		final var result = (MockStructure) transformer.unmarshal().apply(address);
		assertEquals(structure.layout(), result.layout());
		assertEquals(3, result.field);
	}

	// TODO
	// - array of structure
	// - by reference: single, array?
	// - nested structure?

	private static class AnonymousField implements NativeStructure {
    	@Override
    	public StructLayout layout() {
    		return MemoryLayout.structLayout(
    				JAVA_INT,
    				MemoryLayout.paddingLayout(4)
    		);
    	}
	}

    @Test
    void anonymous() {
    	assertThrows(IllegalArgumentException.class, () -> factory.create(AnonymousField.class));
    }

	private static class UnknownField implements NativeStructure {
    	@Override
    	public StructLayout layout() {
    		return MemoryLayout.structLayout(
    				JAVA_INT.withName("cobblers"),
    				MemoryLayout.paddingLayout(4)
    		);
    	}
	}

	@Test
	void unknown() {
    	assertThrows(IllegalArgumentException.class, () -> factory.create(UnknownField.class));
	}

	private static class UnsupportedField implements NativeStructure {
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

	@Test
	void unsupported() {
    	assertThrows(IllegalArgumentException.class, () -> factory.create(UnsupportedField.class));
	}
}
