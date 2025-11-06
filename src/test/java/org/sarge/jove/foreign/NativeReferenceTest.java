package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

class NativeReferenceTest {
	private NativeReference<Integer> reference;

	@BeforeEach
	void before() {
		reference = new NativeReference<>() {
			@Override
			protected Integer update(MemorySegment pointer) {
				return pointer.get(ValueLayout.JAVA_INT, 0L);
			}
		};
	}

	@Test
	void empty() {
		assertEquals(null, reference.get());
	}

	@Test
	void set() {
		reference.set(2);
		assertEquals(2, reference.get());
	}

	@Nested
	class TransformerTest {
    	private NativeReferenceTransformer transformer;
    	private SegmentAllocator allocator;

    	@BeforeEach
    	void before() {
    		allocator = Arena.ofAuto();
    		transformer = new NativeReferenceTransformer();
    	}

    	@Test
    	void layout() {
    		assertEquals(ValueLayout.ADDRESS, transformer.layout());
    	}

    	@Test
    	void empty() {
    		assertEquals(MemorySegment.NULL, transformer.empty());
    	}

    	@Test
    	void marshal() {
    		assertNotNull(transformer.marshal(reference, allocator));
    	}

    	@Test
    	void unmarshal() {
    		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
    	}

    	@Test
    	void update() {
    		final MemorySegment address = allocator.allocate(ValueLayout.ADDRESS);
    		address.set(ValueLayout.JAVA_INT, 0L, 3);
    		transformer.update().accept(address, reference);
    		assertEquals(3, reference.get());
    	}
    }
}
