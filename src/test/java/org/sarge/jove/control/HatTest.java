package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Hat.HatAction;

public class HatTest {
	private static final Set<HatAction> MASK = Set.of(HatAction.RIGHT, HatAction.UP);
	private static final String HAT = "HAT";

	private Hat hat;

	@BeforeEach
	void before() {
		hat = new Hat(HAT, Set.of(HatAction.UP));
	}

	@Test
	void constructor() {
		assertEquals("HAT-UP", hat.name());
		assertEquals("HAT", hat.id());
		assertEquals(Set.of(HatAction.UP), hat.action());
		assertEquals(hat, hat.type());
	}

	@DisplayName("Hat without action mask should be centered")
	@Test
	void centrered() {
		hat = new Hat(HAT);
		assertEquals(Set.of(HatAction.CENTERED), hat.action());
	}

	@DisplayName("Diagonal hat positions are comprised of an action mask")
	@Test
	void diagonal() {
		hat = new Hat(HAT, MASK);
		assertEquals("HAT-UP-RIGHT", hat.name());
		assertEquals("HAT", hat.id());
		assertEquals(MASK, hat.action());
	}

	@DisplayName("Hat templates should match the same id and action mask")
	@Test
	void matches() {
		assertEquals(true, hat.matches(hat));
		assertEquals(false, hat.matches(new Hat(HAT)));
		assertEquals(false, hat.matches(new Hat("other", Set.of(HatAction.UP))));
	}

	@DisplayName("Hat templates should match a hat with a super-set of the action mask")
	@Test
	void matchesMasked() {
		assertEquals(true, hat.matches(new Hat(HAT, MASK)));
	}

	@DisplayName("Button template should match buttons with the same id and action")
	@Test
	void resolve() {
		final Hat resolved = hat.resolve(HatAction.DOWN.value());
		assertNotNull(resolved);
		assertEquals("HAT-DOWN", resolved.name());
		assertEquals(Set.of(HatAction.DOWN), resolved.action());
	}

	@DisplayName("Button template should match buttons with the same id and action")
	@Test
	void resolveInvalidActionMask() {
		assertThrows(IllegalArgumentException.class, () -> hat.resolve(999));
	}
}
