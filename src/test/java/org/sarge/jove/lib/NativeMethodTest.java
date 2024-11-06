package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.NativeMethod.Factory;

class NativeMethodTest {
	interface MockLibrary {
		int		abs(int n);
		void	srand(int seed);
		int		strlen(String string);
		String	strerror(int code);
		void	cobblers();
	}

	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
	}

	@DisplayName("The number of native mappers must match the method signature")
	@Test
	void parameterCount() throws Exception {
		final Method method = MockLibrary.class.getMethod("srand", int.class);
		final MethodHandle handle = MethodHandles.lookup().unreflect(method);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(method, handle, List.of(), null));
	}

	@DisplayName("A native method with a return value must be constructed with an appropriate native mapper")
	@Test
	void missingReturnType() throws Exception {
		final Method method = MockLibrary.class.getMethod("abs", int.class);
		final MethodHandle handle = MethodHandles.lookup().unreflect(method);
		final var mapper = new DefaultNativeMapper(int.class, ValueLayout.JAVA_INT);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(method, handle, List.of(mapper), null));
	}

	@DisplayName("A native method with a void return value should not be constructed with a return type native mapper")
	@Test
	void superfluousReturnType() throws Exception {
		final Method method = MockLibrary.class.getMethod("cobblers");
		final MethodHandle handle = MethodHandles.lookup().unreflect(method);
		final var mapper = new DefaultNativeMapper(int.class, ValueLayout.JAVA_INT);
		assertThrows(IllegalArgumentException.class, () -> new NativeMethod(method, handle, List.of(), mapper));
	}

	@Test
	void invoke() throws Exception {
		// Lookup method
		final Method method = MockLibrary.class.getMethod("abs", int.class);

		// Build native method handle
		final Linker linker = Linker.nativeLinker();
		final MemorySegment abs = linker.defaultLookup().find("abs").get();
		final var descriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
		final MethodHandle handle = linker.downcallHandle(abs, descriptor);

		// Create native method wrapper
		final var mapper = new DefaultNativeMapper(int.class, ValueLayout.JAVA_INT);
		final var wrapper = new NativeMethod(method, handle, List.of(mapper), mapper);

		// Invoke
		assertEquals(2, wrapper.invoke(new Object[]{-2}, arena));
	}

	@Nested
	class FactoryTests {
		private Factory factory;
		private SymbolLookup lookup;
		private NativeMapperRegistry registry;

		@BeforeEach
		void before() {
			registry = new NativeMapperRegistry();
			registry.add(new DefaultNativeMapper(int.class, ValueLayout.JAVA_INT));
			lookup = Linker.nativeLinker().defaultLookup();
			factory = new Factory(lookup, registry);
		}

    	@DisplayName("A native method can be constructed for an API method")
    	@Test
    	void build() throws Exception {
    		final Method method = MockLibrary.class.getMethod("abs", int.class);
    		final NativeMethod wrapper = factory.build(method);
    		assertEquals(2, wrapper.invoke(new Object[]{-2}, arena));
    	}

    	@DisplayName("A native method can be constructed for an API method without a return value")
    	@Test
    	void voidMethod() throws Exception {
    		final Method method = MockLibrary.class.getMethod("srand", int.class);
    		final NativeMethod wrapper = factory.build(method);
    		assertEquals(null, wrapper.invoke(new Object[]{3}, arena));
    	}

    	@DisplayName("A native method cannot be constructed if it does not exist in the given API")
    	@Test
    	void unknown() throws Exception {
    		final Method method = MockLibrary.class.getMethod("cobblers");
    		assertThrows(IllegalArgumentException.class, () -> factory.build(method));
    	}

    	@DisplayName("A native method cannot be constructed for an unsupported parameter type")
    	@Test
    	void unsupportedParameter() throws Exception {
    		final Method method = MockLibrary.class.getMethod("strlen", String.class);
    		assertThrows(IllegalArgumentException.class, () -> factory.build(method));
    	}

    	@DisplayName("A native method cannot be constructed for an unsupported return type")
    	@Test
    	void unsupportedReturnType() throws Exception {
    		final Method method = MockLibrary.class.getMethod("strerror", int.class);
    		assertThrows(IllegalArgumentException.class, () -> factory.build(method));
    	}
	}
}
