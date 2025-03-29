package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;

public class DesktopForeignDemo {
    private final Linker linker = Linker.nativeLinker();
    private final Arena arena;
    private final SymbolLookup lookup;

    public DesktopForeignDemo(Arena arena) {
    	this.arena = arena;
    	this.lookup = SymbolLookup.libraryLookup("C:/GLFW/lib-mingw-w64/glfw3.dll", arena);
    }

    private MemorySegment lookup(String method) {
		return lookup.find(method).orElseThrow(() -> new RuntimeException("Cannot find symbol " + method));
    }

    @SuppressWarnings("unchecked")
	private static <T> T invoke(MethodHandle handle, Object... args) {
    	try {
    		return (T) handle.invokeWithArguments(args);
    	}
    	catch(Throwable t) {
    		throw new RuntimeException(t);
    	}
    }

    private void init() {
        final MethodHandle handle = linker.downcallHandle(
        		lookup("glfwInit"),
        		FunctionDescriptor.of(JAVA_INT)
        );
        final int result = invoke(handle);
        if(result != 1) throw new RuntimeException("Cannot initialise GLFW: " + result);
    }

    private String version() {
        final MethodHandle version = linker.downcallHandle(
        		lookup("glfwGetVersionString"),
        		FunctionDescriptor.of(ADDRESS)
        );
        final MemorySegment result = invoke(version);
        return result.reinterpret(Integer.MAX_VALUE).getString(0L);
    }

    private boolean isVulkanSupported() {
        final MethodHandle supported = linker.downcallHandle(
        		lookup("glfwVulkanSupported"),
        		FunctionDescriptor.of(JAVA_BOOLEAN)
        );
        return invoke(supported);
    }

    private String[] extensions() {
        // Query extensions
    	final MethodHandle extensions = linker.downcallHandle(
        		lookup("glfwGetRequiredInstanceExtensions"),
        		FunctionDescriptor.of(ADDRESS, ADDRESS)
        );
        final MemorySegment count = arena.allocate(JAVA_INT);
        final MemorySegment result = invoke(extensions, count);

        // Count number of extensions
        final int length = count.get(JAVA_INT, 0L);
        final String[] array = new String[length];

        // Convert to array of strings
        final MemorySegment address = result.reinterpret(length * ADDRESS.byteSize());
        for(int n = 0; n < length; ++n) {
            final MemorySegment name = address.getAtIndex(ADDRESS, n);
            array[n] = name.reinterpret(Integer.MAX_VALUE).getString(0L);
        }

        return array;
    }

    private void close() {
        final MethodHandle handle = linker.downcallHandle(
        		lookup("glfwTerminate"),
        		FunctionDescriptor.ofVoid()
        );
        invoke(handle);
    }

	public static void main() {
		try(Arena arena = Arena.ofConfined()) {
			final var demo = new DesktopForeignDemo(arena);
			System.out.println("Initialising...");
			demo.init();
			System.out.println("Version=" + demo.version());
			System.out.println("isVulkanSupported=" + demo.isVulkanSupported());
			System.out.println("Extensions=" + Arrays.toString(demo.extensions()));
			demo.close();
			System.out.println("Done");
		}
        catch(Throwable e) {
        	e.printStackTrace();
        }
	}
}
