package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.reflect.Method;

import org.junit.jupiter.api.*;

class NativeMethodTest {
	interface MockLibrary {
		int abs(int n);
		void srand(int seed);
		int strlen(String string);
		String strerror(int code);
		void cobblers();
	}

	private SymbolLookup lookup;
	private NativeContext context;
	private NativeMethod.Builder builder;

	@BeforeEach
	void before() {
		final var linker = Linker.nativeLinker();
		final var registry = new NativeMapperRegistry();
		registry.add(new IntegerNativeMapper());
		lookup = linker.defaultLookup();
		context = new NativeContext(linker, registry);
		builder = new NativeMethod.Builder(lookup, context);
	}

	@Test
	void build() throws Exception {
		final Method method = MockLibrary.class.getMethod("srand", int.class);
		final NativeMethod wrapper = builder.build(method);
		assertEquals(null, wrapper.invoke(new Object[]{1}));
	}

	@Test
	void buildReturnType() throws Exception {
		final Method method = MockLibrary.class.getMethod("abs", int.class);
		final NativeMethod wrapper = builder.build(method);
		assertEquals(2, wrapper.invoke(new Object[]{-2}));
	}

	@Test
	void unknown() throws Exception {
		final Method method = MockLibrary.class.getMethod("cobblers");
		assertThrows(IllegalArgumentException.class, () -> builder.build(method));
	}

	@Test
	void unsupportedParameter() throws Exception {
		final Method method = MockLibrary.class.getMethod("strlen", String.class);
		assertThrows(IllegalArgumentException.class, () -> builder.build(method));
	}

	@Test
	void unsupportedReturnType() throws Exception {
		final Method method = MockLibrary.class.getMethod("strerror", int.class);
		assertThrows(IllegalArgumentException.class, () -> builder.build(method));
	}
}
