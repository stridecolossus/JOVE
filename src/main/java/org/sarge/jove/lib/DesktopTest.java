package org.sarge.jove.lib;

import java.lang.foreign.*;

public class DesktopTest {

	public static void main(String[] args) {

		interface Desktop {
			int glfwInit();
			void glfwTerminate();
//			void glfwInitHint(int hint, int value);

			String glfwGetVersionString();
			boolean glfwVulkanSupported();
//			Pointer glfwGetRequiredInstanceExtensions(IntByReference count);
		}


		try(final Arena arena = Arena.ofConfined()) {
    		System.out.println("Initialising context...");
    		final var context = new NativeContext();
    		context.registry().add(new IntegerNativeMapper());
    		context.registry().add(new BooleanNativeMapper());

    		System.out.println("Initialising factory...");
    		final var factory = new NativeFactory(context);
    		final var lookup = SymbolLookup.libraryLookup("C:/GLFW/lib-mingw-w64/glfw3.dll", arena);

    		System.out.println("Building API...");
    		final Desktop desktop = factory.build(lookup, Desktop.class);

    		System.out.println("Invoking...");
    		System.out.println("init="+desktop.glfwInit());
    		System.out.println("version="+desktop.glfwGetVersionString());
    		System.out.println("Vulkan="+desktop.glfwVulkanSupported());
    		desktop.glfwTerminate();

    		System.out.println("DONE");
		}
	}
}
