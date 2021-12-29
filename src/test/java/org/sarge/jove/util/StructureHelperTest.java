package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StructureHelperTest {
	private Object obj;
	private BiConsumer<Object, MockStructure> populate;

	@BeforeEach
	void before() {
		obj = new Object();
		populate = mock(BiConsumer.class);
	}

	@Test
	void array() {
		final MockStructure[] array = StructureHelper.array(List.of(obj, obj), MockStructure::new, populate);
		assertNotNull(array);
		assertEquals(2, array.length);
		verify(populate).accept(obj, array[0]);
		verify(populate).accept(obj, array[1]);
	}

	@Test
	void pointer() {
		final MockStructure[] array = new MockStructure[2];
		final MockStructure ptr = StructureHelper.pointer(List.of(obj, obj), MockStructure.ByReference::new, populate);
		assertNotNull(ptr);
		ptr.toArray(array);
		verify(populate).accept(obj, array[0]);
		verify(populate).accept(obj, array[1]);
	}

	@Test
	void pointerNotReference() {
		assertThrows(IllegalArgumentException.class, () -> StructureHelper.pointer(List.of(obj), MockStructure::new, populate));
	}

	@Test
	void collector() {
		// Create structure collector
		final Collector<Object, ?, MockStructure[]> collector = StructureHelper.collector(MockStructure::new, populate);
		assertNotNull(collector);

		// Check collected array
		final MockStructure[] array = Stream.of(obj).collect(collector);
		assertNotNull(array);
		assertEquals(1, array.length);
		verify(populate).accept(obj, array[0]);
	}
}
