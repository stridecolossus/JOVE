package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TupleTest {
	private Tuple tuple;

	@BeforeEach
	void before() {
		tuple = spy(Tuple.class);
	}

	@SuppressWarnings("static-method")
	@Test
	void size() {
		assertEquals(3, Tuple.SIZE);
	}

	@Test
	void length() {
		assertEquals(3 * Float.BYTES, tuple.length());
	}
}
