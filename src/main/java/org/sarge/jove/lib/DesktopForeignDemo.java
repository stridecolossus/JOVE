package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class DesktopForeignDemo {
	private final Linker linker = Linker.nativeLinker();
	private final SymbolLookup lookup;

	public DesktopForeignDemo(SymbolLookup lookup) {
		this.lookup = lookup;
	}

	private Object invoke(String name, Object[] args, MemoryLayout returnType, MemoryLayout... signature) throws Throwable {
		final MemorySegment symbol = lookup.find(name).orElseThrow();
		final var descriptor = returnType == null ? FunctionDescriptor.ofVoid(signature) : FunctionDescriptor.of(returnType, signature);
		final MethodHandle handle = linker.downcallHandle(symbol, descriptor);
		return handle.invokeWithArguments(args);
	}

	public static void main(String[] args) throws Throwable {
		try(final Arena arena = Arena.ofConfined()) {
			// Init lookup service
			final SymbolLookup lookup = SymbolLookup.libraryLookup("C:/GLFW/lib-mingw-w64/glfw3.dll", arena);
			final var demo = new DesktopForeignDemo(lookup);

			// Init GLFW
			System.out.println("glfwInit=" + demo.invoke("glfwInit", null, JAVA_INT));

			// Display GLFW version
			final MemorySegment version = (MemorySegment) demo.invoke("glfwGetVersionString", null, ADDRESS);
			System.out.println("glfwGetVersionString=" + version.reinterpret(Integer.MAX_VALUE).getString(0));

			// Check Vulkan is supported
			System.out.println("glfwVulkanSupported=" + demo.invoke("glfwVulkanSupported", null, ValueLayout.JAVA_BOOLEAN));

			// Query Vulkan extensions
			final MemorySegment ref = arena.allocate(JAVA_INT);
			final MemorySegment extensions = (MemorySegment) demo.invoke("glfwGetRequiredInstanceExtensions", new Object[]{ref}, ADDRESS, ADDRESS);
			final int count = ref.get(JAVA_INT, 0);
			System.out.println("glfwGetRequiredInstanceExtensions=" + count);

			// Extract and display extensions
			final MemorySegment array = extensions.reinterpret(count * ADDRESS.byteSize());
			for(int n = 0; n < count; ++n) {
				final MemorySegment e = array.getAtIndex(ADDRESS, n);
				System.out.println("  " + e.reinterpret(Integer.MAX_VALUE).getString(0));
			}

			// Terminate GLFW
			demo.invoke("glfwTerminate", null, null);
		}
	}
}
