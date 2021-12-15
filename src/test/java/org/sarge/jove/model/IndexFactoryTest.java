package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class IndexFactoryTest {
	@Test
	void strip() {
		// Create index factory
		final IndexFactory factory = spy(IndexFactory.class);
		when(factory.indices(0, 1)).then(inv -> IntStream.of(2));

		// Check strip
		final IntStream indices = factory.strip(1);
		assertNotNull(indices);
		assertArrayEquals(new int[]{2}, indices.toArray());
	}

	@Test
	void simple() {
		final IntStream indices = IndexFactory.DEFAULT.indices(1, 2);
		assertNotNull(indices);
		assertArrayEquals(new int[]{1, 2}, indices.toArray());
	}
}
