package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class IndexFactoryTest {
	@Test
	void increment() {
		final IntStream stream = IndexFactory.increment(1, IntStream.of(2));
		assertNotNull(stream);
		assertArrayEquals(new int[]{3}, stream.toArray());
	}
}
