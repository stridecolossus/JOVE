package org.sarge.jove.lib;

import java.lang.foreign.*;
import java.util.Arrays;

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
			int				glfwInit();
			void			glfwInitHint(TestEnum hint, boolean value); // int, int
			void			glfwTerminate();
			boolean			glfwVulkanSupported();
			String			glfwGetVersionString();
			StringArray		glfwGetRequiredInstanceExtensions(IntegerReference count);
		}

		try(final Arena arena = Arena.ofConfined()) {
    		System.out.println("Initialising...");
    		final var lookup = SymbolLookup.libraryLookup("C:/GLFW/lib-mingw-w64/glfw3.dll", arena);
    		final var factory = new NativeFactory(NativeMapperRegistry.create());

    		System.out.println("Building...");
    		final Desktop desktop = factory.build(lookup, Desktop.class);

    		System.out.println("Invoking...");
//    		desktop.glfwInitHint(0x00050001, true);
    		desktop.glfwInitHint(TestEnum.HINT, true);
    		System.out.println("init="+desktop.glfwInit());
    		System.out.println("Vulkan="+desktop.glfwVulkanSupported());
    		System.out.println("version="+desktop.glfwGetVersionString());

    		final var count = new IntegerReference(arena);
    		final StringArray array = desktop.glfwGetRequiredInstanceExtensions(count);
    		System.out.println("extensions="+Arrays.toString(array.array(count.value())));

    		System.out.println("Invoking...");
    		desktop.glfwTerminate();

    		System.out.println("DONE");
		}
	}
}
