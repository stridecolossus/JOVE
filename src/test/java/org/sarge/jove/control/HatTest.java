package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		hat = new Hat("HAT");
	}

	@Test
	void constructor() {
		assertEquals("HAT-CENTERED", hat.name());
		assertEquals("HAT", hat.id());
		assertEquals(Set.of(HatAction.CENTERED), hat.action());
		assertEquals(hat, hat.type());
	}

	@Test
	void resolve() {
		hat = hat.resolve(IntegerEnumeration.mask(MASK), 0);
		assertEquals("HAT-UP-RIGHT", hat.name());
		assertEquals(MASK, hat.action());
	}

	@Test
	void resolveInvalidModifiers() {
		assertThrows(IllegalArgumentException.class, () -> hat.resolve(1, 2));
	}

	@Test
	void resolveInvalidActionMask() {
		assertThrows(IllegalArgumentException.class, () -> hat.resolve(999, 0));
	}
}
