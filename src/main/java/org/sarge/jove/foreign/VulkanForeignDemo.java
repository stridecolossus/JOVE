package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;

import org.sarge.jove.platform.vulkan.common.Version;

public class VulkanForeignDemo {
    private static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_BYTE));
    private static final MemoryLayout PADDING = MemoryLayout.paddingLayout(4);

    private final Linker linker = Linker.nativeLinker();
    private final Arena arena;
    private final SymbolLookup lookup;

    public VulkanForeignDemo(Arena arena) {
    	this.arena = arena;
    	this.lookup = SymbolLookup.libraryLookup("vulkan-1", arena);
    }

    private MemorySegment lookup(String method) {
		return lookup.find(method).orElseThrow(() -> new RuntimeException("Cannot find symbol " + method));
    }

	private static void invoke(MethodHandle handle, Object... args) {
    	try {
    		final Object result = handle.invokeWithArguments(args);

    		if(handle.type().returnType() == int.class) {
    			final int code = (int) result;
    			if(code != 0) {
    				throw new RuntimeException("Vulkan error %d in method %s".formatted(code, handle));
    			}
    		}
    	}
    	catch(Throwable t) {
    		throw new RuntimeException(t);
    	}
    }

    private MemorySegment applicationDescriptor() {
		final MemoryLayout layout = MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				POINTER.withName("pApplicationName"),
				JAVA_INT.withName("applicationVersion"),
				PADDING,
				POINTER.withName("pEngineName"),
				JAVA_INT.withName("engineVersion"),
				PADDING,
				JAVA_INT.withName("apiVersion"),
				PADDING
		);

		final MemorySegment structure = arena.allocate(layout);

		final long sType = layout.byteOffset(PathElement.groupElement("sType"));
		structure.set(JAVA_INT, sType, 0);

		final long pApplicationName = layout.byteOffset(PathElement.groupElement("pApplicationName"));
		structure.set(ADDRESS, pApplicationName, arena.allocateFrom("TEST"));

		final Version ver = new Version(1, 2, 3);
		final long applicationVersion = layout.byteOffset(PathElement.groupElement("applicationVersion"));
		structure.set(JAVA_INT, applicationVersion, ver.toInteger());

		final long pEngineName = layout.byteOffset(PathElement.groupElement("pEngineName"));
		structure.set(ADDRESS, pEngineName, arena.allocateFrom("JOVE"));

		final Version api = new Version(1, 1, 0);
		final long apiVersion = layout.byteOffset(PathElement.groupElement("apiVersion"));
		structure.set(JAVA_INT, apiVersion, api.toInteger());

		return structure;
    }

    private MemorySegment createInstanceDescriptor() {
		final MemoryLayout layout = MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			PADDING,
    			POINTER.withName("pNext"),
    			JAVA_INT.withName("flags"),
    			PADDING,
    			POINTER.withName("pApplicationInfo"),
    			JAVA_INT.withName("enabledLayerCount"),
    			PADDING,
    			POINTER.withName("ppEnabledLayerNames"),
    			JAVA_INT.withName("enabledExtensionCount"),
    			PADDING,
    			POINTER.withName("ppEnabledExtensionNames")
		);

		final MemorySegment structure = arena.allocate(layout);

		final long sType = layout.byteOffset(PathElement.groupElement("sType"));
		structure.set(JAVA_INT, sType, 1);

		final long pApplicationInfo = layout.byteOffset(PathElement.groupElement("pApplicationInfo"));
		structure.set(ADDRESS, pApplicationInfo, applicationDescriptor());

		final long enabledExtensionCount = layout.byteOffset(PathElement.groupElement("enabledExtensionCount"));
		structure.set(JAVA_INT, enabledExtensionCount, 1);

		final long ppEnabledExtensionNames = layout.byteOffset(PathElement.groupElement("ppEnabledExtensionNames"));
		final MemorySegment extensions = arena.allocate(ADDRESS, 1);
		extensions.setAtIndex(ADDRESS, 0, arena.allocateFrom("VK_EXT_debug_utils"));
		structure.set(ADDRESS, ppEnabledExtensionNames, extensions);

		final long enabledLayerCount = layout.byteOffset(PathElement.groupElement("enabledLayerCount"));
		structure.set(JAVA_INT, enabledLayerCount, 1);

		final long ppEnabledLayerNames = layout.byteOffset(PathElement.groupElement("ppEnabledLayerNames"));
		final MemorySegment layers = arena.allocate(ADDRESS, 1);
		layers.setAtIndex(ADDRESS, 0, arena.allocateFrom("VK_LAYER_KHRONOS_validation"));
		structure.set(ADDRESS, ppEnabledLayerNames, layers);

		return structure;
    }

    private MemorySegment createInstance() {
		final MethodHandle handle = linker.downcallHandle(
				lookup("vkCreateInstance"),
				FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS)
		);

		final MemorySegment structure = createInstanceDescriptor();
        final MemorySegment ref = arena.allocate(ADDRESS);
		invoke(handle, structure, MemorySegment.NULL, ref);

		return ref.get(ADDRESS, 0L);
    }

    /*

    private MemorySegment createDiagnosticHandler(MemorySegment instance) throws Throwable {

		final MethodHandle handle = linker.downcallHandle(
				lookup("vkGetInstanceProcAddr"),
				FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS)
		);

		final MemorySegment create = (MemorySegment) handle.invoke(instance, arena.allocateFrom("vkCreateDebugUtilsMessengerEXT"));

		final var type = MethodType.methodType(
				boolean.class,
				int.class, int.class, MemorySegment.class, MemorySegment.class
		);

		final MethodHandle callbackHandle = MethodHandles.lookup().findStatic(VulkanForeignDemo.class, "message", type);
		final var descriptor = FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS);
		final MemorySegment stub = linker.upcallStub(callbackHandle, descriptor, arena);

		final MemoryLayout layout = MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("messageSeverity"),
				JAVA_INT.withName("messageType"),
				PADDING,
				POINTER.withName("pfnUserCallback"),
				POINTER.withName("pUserData")
		);

		final MemorySegment structure = arena.allocate(layout);

		final long sType = layout.byteOffset(PathElement.groupElement("sType"));
		structure.set(JAVA_INT, sType, 1000128004);

		final long messageSeverity = layout.byteOffset(PathElement.groupElement("messageSeverity"));
		structure.set(JAVA_INT, messageSeverity, 1); // verbose

		final long messageType = layout.byteOffset(PathElement.groupElement("messageType"));
		structure.set(JAVA_INT, messageType, 1 | 2 | 4); // all

		final long pfnUserCallback = layout.byteOffset(PathElement.groupElement("pfnUserCallback"));
		structure.set(ADDRESS, pfnUserCallback, stub);

		final MethodHandle createHandle = linker.downcallHandle(
				create,
				FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS)
		);

        final MemorySegment ref = arena.allocate(ADDRESS);
		invoke(createHandle, instance, structure, MemorySegment.NULL, ref);

		return ref.get(ADDRESS, 0L);
    }

    private void destroyDiagnosticHandler(MemorySegment instance, MemorySegment handler) throws Throwable {

		final MethodHandle handle = linker.downcallHandle(
				lookup("vkGetInstanceProcAddr"),
				FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS)
		);

		final MemorySegment destroy = (MemorySegment) handle.invoke(instance, arena.allocateFrom("vkDestroyDebugUtilsMessengerEXT"));

		final MethodHandle destroyHandle = linker.downcallHandle(
				destroy,
				FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS)
		);

		invoke(destroyHandle, instance, handler, MemorySegment.NULL);
    }

    private static boolean message(int severity, int typeMask, MemorySegment pCallbackData, MemorySegment pUserData) {
    	System.err.println("ERROR!");
    	return true;
    }

    */

    private void enumerate(MemorySegment instance) {
		final MethodHandle handle = linker.downcallHandle(
				lookup("vkEnumeratePhysicalDevices"),
				FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS)
		);

		final MemorySegment count = arena.allocate(JAVA_INT);

		invoke(handle, instance, count, MemorySegment.NULL);

		final MemorySegment ref = arena.allocate(ADDRESS);
		invoke(handle, instance, count, ref);

		System.out.println("devices=" + count.get(JAVA_INT, 0L));
    }

    private void destroyInstance(MemorySegment instance) {
		final MethodHandle handle = linker.downcallHandle(
				lookup("vkDestroyInstance"),
				FunctionDescriptor.ofVoid(ADDRESS, ADDRESS)
		);
		invoke(handle, instance, MemorySegment.NULL);
    }

    public static void main() {
		System.out.println("Starting...");

		try(Arena arena = Arena.ofConfined()) {
			final var demo = new VulkanForeignDemo(arena);

			System.out.println("Creating instance...");
			final MemorySegment instance = demo.createInstance();

//			System.out.println("Creating diagnostic handler...");
//			final MemorySegment handler = demo.createDiagnosticHandler(instance);

			System.out.println("Enumerating physical devices...");
			demo.enumerate(instance);

//			System.out.println("Destroying handler...");
//			demo.destroyDiagnosticHandler(instance, handler);

			System.out.println("Destroying instance...");
			demo.destroyInstance(instance);

			System.out.println("Done");
		}
        catch(Throwable e) {
        	e.printStackTrace();
        }
	}
}
