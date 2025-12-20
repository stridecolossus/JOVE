package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.Mockery.Mock;

class MockeryTest {
	interface MockInterface {
		int strlen(String string);
	}

	private Mockery mockery;
	private MockInterface proxy;
	private Mock mock;

	@BeforeEach
	void before() {
		mockery = new Mockery(MockInterface.class);
		proxy = mockery.proxy();
		mock = mockery.mock("strlen");
	}

	@DisplayName("A mocked method...")
	@Nested
	class MockMethodTest {
		@DisplayName("initially has zero invocations")
		@Test
		void mock() {
			assertEquals(0, mock.count());
			assertEquals(List.of(), mock.arguments());
		}

		@DisplayName("can be invoked")
		@Test
		void invoke() {
			proxy.strlen("string");
			assertEquals(1, mock.count());
			assertEquals(List.of("string"), mock.arguments());
		}

		@DisplayName("can be configured to fail")
		@Test
		void fail() {
			final var exception = new RuntimeException("doh") {
				// Custom
			};
			mock.fail(exception);
			assertThrows(exception.getClass(), () -> proxy.strlen("string"));
			assertEquals(1, mock.count());
			assertEquals(List.of("string"), mock.arguments());
		}

		@DisplayName("cannot be created for an unknown API method")
		@Test
		void unknown() {
			assertThrows(IllegalArgumentException.class, () -> mockery.mock("unknown"));
		}

		@DisplayName("cannot be created for an ambiguous method name")
		@Test
		void ambiguous() {
			interface Overloaded {
				void method(int n);
				void method(float f);
			}
			assertThrows(IllegalArgumentException.class, () -> new Mockery(Overloaded.class).mock("method"));
		}

		@Nested
		class ReturnValueTest {
			@DisplayName("returns the appropriate default value")
			@Test
			void defaultValue() {
				assertEquals(0, proxy.strlen("string"));
			}

			@DisplayName("can configure the return value")
			@Test
			void result() {
				mock.result(42);
				assertEquals(42, proxy.strlen("string"));
			}

			@DisplayName("can configure a primitive return value")
			@Test
			void wrapper() {
				mock.result(Integer.valueOf(42));
				assertEquals(42, proxy.strlen("string"));
			}

			@DisplayName("must return the correct type")
			@Test
			void type() {
				assertThrows(ClassCastException.class, () -> mock.result(false));
			}

			@DisplayName("cannot configure the return value of a void method")
			@Test
			void voidMethod() {
				interface VoidMethod {
					void method();
				}
				final var mockery = new Mockery(VoidMethod.class);
				assertThrows(IllegalArgumentException.class, () -> mockery.mock("method").result(42));
			}
		}
	}

	@DisplayName("An API method with a concrete implementation...")
	@Nested
	class ConcreteMethodTest {
		// Concrete implementations can be hidden
		private class Concrete {
			@SuppressWarnings("unused")
			public int strlen(String string) {
				return string.length();
			}
		}

		@BeforeEach
		void before() {
			mockery.implement(new Concrete());
		}

		@DisplayName("delegates to that implementation")
		@Test
		void invoke() {
			assertEquals(6, proxy.strlen("string"));
			assertEquals(1, mock.count());
		}

		@DisplayName("can override the return value")
		@Test
		void result() {
			mock.result(42);
			assertEquals(42, proxy.strlen("string"));
		}

		@DisplayName("can be configured to fail")
		@Test
		void fail() {
			mock.fail(new RuntimeException());
			assertThrows(RuntimeException.class, () -> proxy.strlen("string"));
		}

		@DisplayName("cannot be configured for an unknown API method")
		@Test
		void unknown() {
			assertThrows(IllegalArgumentException.class, () -> mockery.implement(new Object()));
		}

		@DisplayName("can be overridden more than once")
		@Test
		void duplicate() {
			mockery.implement(new Concrete());
		}
	}
}
