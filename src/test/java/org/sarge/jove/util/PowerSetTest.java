package org.sarge.jove.util;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import javax.lang.model.element.Modifier;

import org.junit.jupiter.api.Test;

public class PowerSetTest {
	@Test
	public void bitfields() {
		assertArrayEquals(IntStream.range(0, 8).toArray(), PowerSet.power(3).toArray());
	}

	@Test
	public void powerSet() {
		final var set = List.of(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE);
		final List<Set<Modifier>> power = PowerSet.power(set).collect(toList());
		final List<Set<Modifier>> expected = List.of(
			Set.of(),
			Set.of(Modifier.PUBLIC),
			Set.of(Modifier.PROTECTED),
			Set.of(Modifier.PUBLIC, Modifier.PROTECTED),
			Set.of(Modifier.PRIVATE),
			Set.of(Modifier.PUBLIC, Modifier.PRIVATE),
			Set.of(Modifier.PRIVATE, Modifier.PROTECTED),
			Set.of(Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED)
		);
		assertEquals(expected, power);
	}
}
