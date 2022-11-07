package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.*;

import org.junit.jupiter.api.*;

public class StructureCollectorTest {
	private Object obj;
	private BiConsumer<Object, MockStructure> populate;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		obj = new Object();
		populate = mock(BiConsumer.class);
	}

	@Test
	void array() {
		final MockStructure[] array = StructureCollector.array(List.of(obj, obj), new MockStructure(), populate);
		assertNotNull(array);
		assertEquals(2, array.length);
		verify(populate).accept(obj, array[0]);
		verify(populate).accept(obj, array[1]);
	}

	@Test
	void pointer() {
		final MockStructure[] array = new MockStructure[2];
		final MockStructure ptr = StructureCollector.pointer(List.of(obj, obj), new MockStructure(), populate);
		assertNotNull(ptr);
		ptr.toArray(array);
		verify(populate).accept(obj, array[0]);
		verify(populate).accept(obj, array[1]);
	}

	@Test
	void collector() {
		// Create structure collector
		final Collector<Object, ?, MockStructure[]> collector = StructureCollector.collector(new MockStructure(), populate);
		assertNotNull(collector);

		// Check collected array
		final MockStructure[] array = Stream.of(obj).collect(collector);
		assertNotNull(array);
		assertEquals(1, array.length);
		verify(populate).accept(obj, array[0]);
	}
}
