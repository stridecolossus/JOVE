package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;

import org.sarge.jove.platform.vulkan.common.Version;

public class VulkanForeignTest {
    private static final AddressLayout POINTER = ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, JAVA_BYTE));

	public static void main(String[] args) throws Throwable {
		try(final Arena arena = Arena.ofConfined()) {
			final var lookup = SymbolLookup.libraryLookup("vulkan-1", arena);
			final var test = new VulkanForeignTest(lookup, arena);
			//test.createInstance();
			test.enumerateExtensions();
		}
	}

	private final Arena arena;
	private final SymbolLookup lookup;
	private final Linker linker = Linker.nativeLinker();

	public VulkanForeignTest(SymbolLookup lookup, Arena arena) {
		this.lookup = lookup;
		this.arena = arena;
	}


	public void enumerateExtensions() {
		final MemorySegment symbol = lookup.find("vkEnumerateInstanceExtensionProperties").orElseThrow();

		final var descriptor = FunctionDescriptor.of(
				JAVA_INT,
				ADDRESS,			// pLayerName
				ADDRESS,			// pPropertyCount
				ADDRESS				// pProperties
		);

		final MethodHandle handle = linker.downcallHandle(symbol, descriptor);

		final MemorySegment count = arena.allocate(ADDRESS);

		System.out.println("vkEnumerateInstanceExtensionProperties.count=" + invoke(handle, MemorySegment.NULL, count, MemorySegment.NULL));
		System.out.println("count=" + count.get(JAVA_INT, 0));

		final StructLayout layout = extensionLayout();

		MemorySegment array = arena.allocate(layout, 16);
		System.out.println("vkEnumerateInstanceExtensionProperties.enumerate=" + invoke(handle, MemorySegment.NULL, count, array));

		final VarHandle name = layout.varHandle(PathElement.groupElement("extensionName"), PathElement.sequenceElement());
		final VarHandle ver = layout.varHandle(PathElement.groupElement("specVersion"));

		array
				.elements(layout)
				.map(e -> e.getString(0))
//				.map(e -> name.get(e, 0, (long) 0)) // , (long) 3))
				.forEach(e -> System.out.println(e));
	}

	private static StructLayout extensionLayout() {
		return MemoryLayout.structLayout(
				MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("extensionName"),
				JAVA_INT.withName("specVersion")
		);
	}

	public void createInstance() throws Throwable {
		final MemorySegment symbol = lookup.find("vkCreateInstance").orElseThrow();

		final var descriptor = FunctionDescriptor.of(
				JAVA_INT,
				ADDRESS,
				ADDRESS,
				ADDRESS
		);

		final MethodHandle handle = linker.downcallHandle(symbol, descriptor);

		final StructLayout appLayout = applicationLayout();
		final MemorySegment app = arena.allocate(appLayout);
		set(appLayout, app, "apiVersion", new Version(9, 9, 9).toInteger());

		final StructLayout infoLayout = createInstanceLayout();
		final MemorySegment info = arena.allocate(infoLayout);
		set(infoLayout, info, "pApplicationInfo", app);

		final MemorySegment ref = arena.allocate(ValueLayout.ADDRESS);
		System.out.println("before="+ref.get(ADDRESS, 0));

		final Object result = invoke(handle, info, MemorySegment.NULL, ref);

		System.out.println("create="+result);
		System.out.println("after="+ref.get(ADDRESS, 0));

		// address.get(AddressLayout.ADDRESS, 0)
	}

	private static void set(StructLayout layout, MemorySegment struct, String name, Object value) {
		final PathElement path = MemoryLayout.PathElement.groupElement(name);
		final VarHandle handle = layout.varHandle(path);
		handle.set(struct, 0, value);
	}

	private static StructLayout applicationLayout() {
		return MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			MemoryLayout.paddingLayout(4),
    			POINTER.withName("pNext"),
    			POINTER.withName("pApplicationName"),
    			JAVA_INT.withName("applicationVersion"),
    			MemoryLayout.paddingLayout(4),
    			POINTER.withName("pEngineName"),
    			JAVA_INT.withName("engineVersion"),
    			MemoryLayout.paddingLayout(4),
    			JAVA_INT.withName("apiVersion"),
    			MemoryLayout.paddingLayout(4)
    	);
	}

	private static StructLayout createInstanceLayout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				MemoryLayout.paddingLayout(4),
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				MemoryLayout.paddingLayout(4),
				POINTER.withName("pApplicationInfo"),
				JAVA_INT.withName("enabledLayerCount"),
				MemoryLayout.paddingLayout(4),
				POINTER.withName("ppEnabledLayerNames"),
				JAVA_INT.withName("enabledExtensionCount"),
				MemoryLayout.paddingLayout(4),
				POINTER.withName("ppEnabledExtensionNames")
		);
	}

	private static Object invoke(MethodHandle handle, Object... args) {
		try {
			return handle.invokeWithArguments(args);
		}
		catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
