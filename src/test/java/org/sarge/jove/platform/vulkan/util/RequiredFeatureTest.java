package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature.Processor;

class RequiredFeatureTest {
	@Nested
	class ProcessorTests {
		@SuppressWarnings("unused")
		public static class MockObject {
			private Object field;

			@RequiredFeature(field="field", feature="feature")
			public void method(Object field) {
				this.field = field;
			}
		}

		private Processor proc;
		private MockObject obj;

		@BeforeEach
		void before() {
			proc = new Processor();
			obj = new MockObject();
		}

		@DisplayName("A device feature is not required for NULL values")
		@Test
		void none() {
			assertEquals(0, proc.enumerate(obj).count());
		}

		@DisplayName("A device feature is not required for FALSE values")
		@Test
		void bool() {
			obj.method(false);
			assertEquals(0, proc.enumerate(obj).count());
		}

		@DisplayName("A device feature is required for TRUE values")
		@Test
		void booleanRequired() {
			obj.method(true);
			assertArrayEquals(new String[]{"feature"}, proc.enumerate(obj).toArray());
		}

		@DisplayName("A device feature is not required for integer values less-than-or-equal to the predicate")
		@Test
		void number() {
			obj.method(1);
			assertEquals(0, proc.enumerate(obj).count());
		}

		@DisplayName("A device feature is required for integer values greater-than the predicate")
		@Test
		void numberRequired() {
			obj.method(2);
			assertArrayEquals(new String[]{"feature"}, proc.enumerate(obj).toArray());
		}

		@DisplayName("Only numeric and boolean fields are supported")
		@Test
		void unsupported() {
			obj.method("doh");
			assertThrows(UnsupportedOperationException.class, () -> proc.enumerate(obj).count());
		}
	}
}
