package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Hat.HatAction;
import org.sarge.jove.util.IntegerEnumeration;

public class HatTest {
	private static final Set<HatAction> MASK = Set.of(HatAction.UP, HatAction.RIGHT);

	private Hat hat;

	@BeforeEach
	void before() {
		hat = new Hat(1, IntegerEnumeration.mask(MASK));
	}

	@Test
	void constructor() {
		assertEquals("Hat-1-UP-RIGHT", hat.name());
		assertEquals(1, hat.id());
		assertEquals(MASK, hat.action());
		assertEquals(hat, hat.type());
	}

	@Test
	void centered() {
		hat = new Hat(1, 0);
		assertEquals("Hat-1-CENTERED", hat.name());
		assertEquals(Set.of(HatAction.CENTERED), hat.action());
	}

	@Test
	void resolve() {
		hat = hat.resolve(HatAction.DOWN.value());
		assertEquals("Hat-1-DOWN", hat.name());
		assertEquals(Set.of(HatAction.DOWN), hat.action());
	}
}
