package org.sarge.jove.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.AudioService.Range;

public class AudioServiceTest {
	@Nested
	class RangeTests {
		private Range range;

		@BeforeEach
		public void before() {
			range = new Range(1, 3);
		}

		@Test
		public void constructor() {
			assertFloatEquals(1, range.min());
			assertFloatEquals(3, range.max());
		}

		@Test
		public void contains() {
			assertEquals(true, range.contains(1));
			assertEquals(true, range.contains(2));
			assertEquals(true, range.contains(3));
			assertEquals(false, range.contains(0));
			assertEquals(false, range.contains(4));
		}

		@Test
		public void equals() {
			assertEquals(true, range.equals(range));
			assertEquals(false, range.equals(null));
			assertEquals(false, range.equals(new Range(4, 5)));
		}
	}
}
