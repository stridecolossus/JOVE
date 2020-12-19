package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StructureCollectorTest {
	private StructureCollector<Object, MockStructure> collector;
	private BiConsumer<Object, MockStructure> populate;
	private Object obj;
	private List<Object> list;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		obj = new Object();
		list = new ArrayList<>();
		populate = mock(BiConsumer.class);
		collector = new StructureCollector<>(MockStructure::new, populate);
	}

	@Test
	void constructor() {
		assertNotNull(collector.supplier());
		assertNotNull(collector.accumulator());
		assertNotNull(collector.combiner());
		assertNotNull(collector.finisher());
		assertEquals(Set.of(), collector.characteristics());
	}

	@Test
	void supplier() {
		final var list = collector.supplier().get();
		assertNotNull(list);
		assertEquals(true, list.isEmpty());
	}

	@Test
	void accumulator() {
		collector.accumulator().accept(list, obj);
		assertEquals(List.of(obj), list);
	}

	@Test
	void combiner() {
		collector.combiner().apply(list, List.of(obj));
		assertEquals(List.of(obj), list);
	}

	@Test
	void finisher() {
		final var array = collector.finisher().apply(List.of(obj));
		assertNotNull(array);
		assertEquals(1, array.length);
		assertNotNull(array[0]);
	}

	@Test
	void finisherEmpty() {
		final var array = collector.finisher().apply(List.of());
		assertEquals(null, array);
	}

	@Test
	void collect() {
		// Collect JNA structures
		final MockStructure[] array = Stream.of(obj, obj).collect(collector);
		assertNotNull(array);

		// Check array elements
		assertEquals(2, array.length);
		assertNotNull(array[0]);
		assertNotNull(array[1]);

		// Check elements are populated
		verify(populate).accept(obj, array[0]);
		verify(populate).accept(obj, array[1]);
	}

	@Test
	void toArray() {
		final MockStructure result = StructureCollector.toArray(List.of(obj), MockStructure::new, populate);
		assertNotNull(result);
		verify(populate).accept(obj, result);
	}

	@Test
	void toArrayEmpty() {
		final MockStructure result = StructureCollector.toArray(List.of(), MockStructure::new, populate);
		assertEquals(null, result);
	}
}
