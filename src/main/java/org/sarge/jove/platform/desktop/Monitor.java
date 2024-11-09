package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.List;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.Desktop.MainThread;
import org.sarge.jove.platform.desktop.DesktopLibraryMonitor.DesktopDisplayMode;

/**
 * A <i>monitor</i> describes a physical monitor attached to this system.
 * @author Sarge
 */
public record Monitor(Handle handle, String name, Dimensions size, List<Monitor.DisplayMode> modes) implements NativeObject {
	/**
	 * Display mode.
	 */
	public record DisplayMode(Dimensions size, List<Integer> depth, int refresh) {
		/**
		 * Constructor.
		 * @param size			Size
		 * @param depth			RGB bit depth
		 * @param refresh		Refresh rate (Hz)
		 * @throws IllegalArgumentException if the given bit depth is not an RGB array
		 */
		public DisplayMode {
			if(depth.size() != 3) throw new IllegalArgumentException("Invalid RGB bit depth array");
			requireNonNull(size);
			requireOneOrMore(refresh);
			depth = List.copyOf(depth);
		}

		/**
		 * Converts a GLFW structure to a display mode.
		 */
		private static DisplayMode of(DesktopDisplayMode mode) {
			final List<Integer> depth = List.of(mode.red, mode.green, mode.blue);
			return new DisplayMode(new Dimensions(mode.width, mode.height), depth, mode.refresh);
		}
	}

	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param name		Monitor name
	 * @param size		Physical dimensions
	 * @param modes		Display modes supported by this monitor
	 */
	public Monitor {
		requireNonNull(handle);
		requireNotEmpty(name);
		requireNonNull(size);
		modes = List.copyOf(modes);
	}

	/**
	 * Retrieves the current display mode for this monitor.
	 * @param desktop Desktop service
	 * @return Current display mode
	 */
	@MainThread
	public DisplayMode mode(Desktop desktop) {
//		final DesktopDisplayMode mode = desktop.library().glfwGetVideoMode(this);
//		return DisplayMode.of(mode);
		return null;
	}

	/**
	 * Enumerates the monitors attached to this system.
	 * The <i>primary</i> monitor is the first in the array.
	 * @param desktop Desktop service
	 * @return Monitors
	 */
	@MainThread
	public static List<Monitor> monitors(Desktop desktop) {
//		// Enumerate monitors
//		final DesktopLibraryMonitor lib = desktop.library();
//		final ReferenceFactory factory = desktop.factory();
//		final IntByReference count = factory.integer();
//		final Pointer[] monitors = lib.glfwGetMonitors(count).getPointerArray(0, count.getValue());
//
//		// Creates a monitor
//		final Function<Pointer, Monitor> create = ptr -> {
//			// Retrieve monitor name
//			final String name = lib.glfwGetMonitorName(ptr);
//
//			// Retrieve monitor dimensions
//			final IntByReference w = factory.integer();
//			final IntByReference h = factory.integer();
//			lib.glfwGetMonitorPhysicalSize(ptr, w, h);		// TODO - millimetres?
//
//			// Retrieve display modes
//			final DesktopDisplayMode first = lib.glfwGetVideoModes(ptr, count);
//			final DesktopDisplayMode[] array = (DesktopDisplayMode[]) first.toArray(count.getValue());
//			final List<DisplayMode> modes = Arrays.stream(array).map(DisplayMode::of).toList();
//
//			// Create monitor
//			final var size = new Dimensions(w.getValue(), h.getValue());
//			return new Monitor(new Handle(ptr), name, size, modes);
//		};
//
//		// Create monitors
//		return Arrays
//				.stream(monitors)
//				.map(create)
//				.toList();
		return null;
	}
}
