package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Hat.HatAction;

public class HatTest {
	private static final Set<HatAction> MASK = Set.of(HatAction.RIGHT, HatAction.UP);
	private static final String HAT = "HAT";

	private Hat hat;

	@BeforeEach
	void before() {
		hat = new Hat(HAT, MASK);
	}

	@Test
	void constructor() {
		assertEquals("HAT-UP-RIGHT", hat.name());
		assertEquals("HAT", hat.id());
		assertEquals(MASK, hat.action());
		assertEquals(hat, hat.type());
	}

	@Test
	void centrered() {
		hat = new Hat(HAT);
		assertEquals(Set.of(HatAction.CENTERED), hat.action());
	}

	@Test
	void matches() {
		assertEquals(true, hat.matches(hat));
		assertEquals(false, hat.matches(new Hat(HAT)));
		assertEquals(false, hat.matches(new DefaultButton("other")));
	}

	@Test
	void matchesAction() {
		assertEquals(true, new Hat(HAT, Set.of()).matches(hat));
		assertEquals(true, new Hat(HAT, MASK).matches(hat));
		assertEquals(false, new Hat(HAT, Set.of(HatAction.DOWN)).matches(hat));
	}

	@Test
	void resolve() {
		final Hat resolved = hat.resolve(HatAction.DOWN.value());
		assertNotNull(resolved);
		assertEquals("HAT-DOWN", resolved.name());
		assertEquals(Set.of(HatAction.DOWN), resolved.action());
	}

	@Test
	void resolveInvalidActionMask() {
		assertThrows(IllegalArgumentException.class, () -> hat.resolve(999));
	}
}
