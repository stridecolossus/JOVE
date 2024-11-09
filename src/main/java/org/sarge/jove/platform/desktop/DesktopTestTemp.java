package org.sarge.jove.platform.desktop;

import java.util.Arrays;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.desktop.Window.Hint;

public class DesktopTestTemp {

	public static void main(String[] args) {
		System.out.println("Initialising...");
		final var desktop = Desktop.create();

		System.out.println("version=" + desktop.version());
		System.out.println("Vulkan=" + desktop.isVulkanSupported());
		System.out.println("extensions=" + Arrays.toString(desktop.extensions()));

		System.out.println("Opening window...");
		final Window window = new Window.Builder()
				.title("DesktopTestTemp")
				.size(new Dimensions(1024, 768))
				.hint(Hint.CLIENT_API, 0)
				.build(desktop);

		System.out.println("Cleanup...");
		window.destroy();
		desktop.destroy();

		System.out.println("Done...");
	}
}
