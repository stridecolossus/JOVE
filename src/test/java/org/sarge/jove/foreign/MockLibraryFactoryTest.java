package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.MockLibraryFactory.MockedMethod;

class MockLibraryFactoryTest {

	private interface MockLibrary {
		int method(int arg);
		void voidMethod();
	}

	private MockLibraryFactory factory;
	private MockLibrary lib;
	private MockedMethod method;

	@BeforeEach
	void before() {
		factory = new MockLibraryFactory(MockLibrary.class);
		lib = factory.proxy();
		method = factory.get("method", int.class);
	}

	@DisplayName("A mocked method is initially unused")
	@Test
	void get() {
		assertEquals(0, method.count());
		assertEquals(0, method.failures());
	}

	@DisplayName("An unknown API method cannot be mocked")
	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> factory.get("cobblers"));
		assertThrows(IllegalArgumentException.class, () -> factory.get("cobblers", int.class));
	}

	@DisplayName("The number of invocations of a mocked method can be counted")
	@Test
	void invoke() {
		assertEquals(0, lib.method(2));
		assertEquals(1, method.count());
		assertEquals(0, method.failures());
	}

	@DisplayName("The return value of a method can be mocked")
	@Test
	void returns() {
		method.returns(3);
		assertEquals(3, lib.method(2));
	}

	@DisplayName("The mocked return value of method cannot be reset to the default result")
	@Test
	void reset() {
		method.returns(3);
		method.returns(null);
		assertEquals(0, lib.method(2));
	}

	@DisplayName("The return value of a void method cannot be mocked")
	@Test
	void voidMethod() {
		final MockedMethod voidMethod = factory.get("voidMethod");
		assertThrows(IllegalArgumentException.class, () -> voidMethod.returns(2));
	}

	@DisplayName("The return value of a mocked method must match the expected return type")
	@Test
	void returnsInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> method.returns("invalid"));
	}

	@DisplayName("A method can be mocked to throw an exception")
	@Test
	void fail() {
		method.fail(new RuntimeException("doh"));
		assertThrows(RuntimeException.class, () -> lib.method(2));
		assertEquals(1, method.count());
		assertEquals(1, method.failures());
	}
}
