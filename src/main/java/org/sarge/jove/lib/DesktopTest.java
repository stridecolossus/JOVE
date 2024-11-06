package org.sarge.jove.lib;

import java.lang.foreign.*;

import org.sarge.jove.util.IntEnum;

public class DesktopTest {

	public static void main(String[] args) {

		enum TestEnum implements IntEnum {
			HINT;

			@Override
			public int value() {
				return 0x00050001;
			}
		}

		interface Desktop {
//			void glfwInitHint(int hint, boolean value);
			int glfwInit();
			void glfwInitHint(TestEnum hint, boolean value);
			void glfwTerminate();
			boolean glfwVulkanSupported();
			String glfwGetVersionString();
			Handle glfwGetRequiredInstanceExtensions(IntegerReference count);
//			Pointer glfwGetRequiredInstanceExtensions(IntByReference count);
		}


		try(final Arena arena = Arena.ofConfined()) {
//    		System.out.println("Initialising context...");
//    		final var registry = new NativeMapperRegistry();
//    		registry.add(new DefaultNativeMapper(int.class, ValueLayout.JAVA_INT));
//    		registry.add(new DefaultNativeMapper(boolean.class, ValueLayout.JAVA_BOOLEAN));
//    		registry.add(new IntEnumNativeMapper());
//    		registry.add(new StringNativeMapper());

    		System.out.println("Initialising factory...");
    		final var lookup = SymbolLookup.libraryLookup("C:/GLFW/lib-mingw-w64/glfw3.dll", arena);
    		final var factory = new NativeFactory();

    		System.out.println("Building API...");
    		final Desktop desktop = factory.build(lookup, Desktop.class);

    		System.out.println("Invoking...");

//    		desktop.glfwInitHint(0x00050001, true);
    		desktop.glfwInitHint(TestEnum.HINT, true);
    		System.out.println("init="+desktop.glfwInit());
    		System.out.println("Vulkan="+desktop.glfwVulkanSupported());
    		System.out.println("version="+desktop.glfwGetVersionString());

    		final var count = new IntegerReference(arena);
    		final Handle handle = desktop.glfwGetRequiredInstanceExtensions(count);

    		System.out.println("strings...");

    		final MemorySegment seg = handle.address().reinterpret(count.value() * ValueLayout.ADDRESS.byteSize());
    		for(int n = 0; n < count.value(); ++n) {
    			final String str = seg.getAtIndex(ValueLayout.ADDRESS, n)
    					.reinterpret(Integer.MAX_VALUE)
    					.getString(0);
    			System.out.println(str);
    		}

    		//handle.address().elements(ValueLayout.ADDRESS).forEach(System.out::println);

    		desktop.glfwTerminate();

    		System.out.println("DONE");
		}
	}
}
