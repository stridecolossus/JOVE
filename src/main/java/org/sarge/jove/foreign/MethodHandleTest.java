package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;
import java.lang.invoke.*;

public class MethodHandleTest {

	public static void main(String[] args) throws Throwable {
		final Linker linker = Linker.nativeLinker();

		final SymbolLookup lookup = linker.defaultLookup();
		final MemorySegment symbol = lookup.find("abs").orElseThrow();

		final FunctionDescriptor descriptor = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
		final MethodHandle handle = linker.downcallHandle(symbol, descriptor);
		System.out.println(handle.invoke(-3));

//		final Parser parser = new Parser();
//		final var type = MethodType.methodType(int.class, String.class);
//		final MethodHandle method = MethodHandles.lookup().findVirtual(Parser.class, "parse", type);
//		final MethodHandle adapter = MethodHandles.filterArguments(handle, 0, method.bindTo(parser));
//		System.out.println(adapter.invoke("-3"));

		final Parser parser = new Parser();
		final MethodHandle method = MethodHandles.lookup().bind(parser, "binary", MethodType.methodType(String.class, int.class));
		final MethodHandle adapter = MethodHandles.filterReturnValue(handle, method);
		System.out.println(adapter.invoke(-3));
	}

	static class Parser {
//		public int parse(String str) {
//			return Integer.parseInt(str);
//		}

		public String binary(int result) {
			return Integer.toBinaryString(result);
		}
	}

//	static int parse(String str) {
//		return Integer.parseInt(str);
//	}
}
