package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class MethodHandlesTest {

//	public static class Transformer {
//		public int transform(String arg) {
//			System.out.println("arg=" + arg);
//			return Integer.parseInt(arg);
//		}
//	}

	public interface Transformer<T> {
		int transform(T arg);
	}

	public static class MockTransformer implements Transformer<String> {
		@Override
		public int transform(String arg) {
			System.out.println("arg=" + arg);
			return Integer.parseInt(arg);
		}
	}

	public static String returns(int arg) {
		return "result(" + arg + ")";
	}

	//@Test
	void test2() throws Throwable {

		final MethodHandles.Lookup lookup2 = MethodHandles.privateLookupIn(Transformer.class, MethodHandles.lookup());

		final Linker linker = Linker.nativeLinker();
		final SymbolLookup lookup = linker.defaultLookup();
		final MemorySegment symbol = lookup.find("abs").orElseThrow();
		final FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
		final MethodHandle handle = linker.downcallHandle(symbol, descriptor);

		final var transformer = new MockTransformer();

		final MethodType type = MethodType.methodType(int.class, String.class);
//		final MethodHandle filter = MethodHandles.publicLookup().bind(transformer, "transform", type);
		final MethodHandle filter = lookup2.bind(transformer, "transform", type);
		final MethodHandle method = MethodHandles.filterArguments(handle, 0, filter);

		final MethodHandle returns = MethodHandles.publicLookup().findStatic(MethodHandlesTest.class, "returns", MethodType.methodType(String.class, int.class));
		final MethodHandle method2 = MethodHandles.filterReturnValue(method, returns);

		System.out.println("invoke=" + method2.invokeWithArguments(new Object[]{"-42"}));

	}

	@Test
	void test() throws Throwable {
		final Method method = MethodHandlesTest.class.getMethod("returns", int.class);
		final MethodHandle handle = MethodHandles.publicLookup().unreflect(method);
		System.out.println("type="+handle.type());
		System.out.println(handle.invoke(42));
	}
}
