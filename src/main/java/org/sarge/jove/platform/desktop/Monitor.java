package org.sarge.jove.platform.desktop;

import java.util.*;
import java.util.function.Function;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.DesktopLibraryMonitor.DesktopDisplayMode;
import org.sarge.jove.util.ReferenceFactory;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

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
			Check.notNull(size);
			Check.oneOrMore(refresh);
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
		Check.notNull(handle);
		Check.notEmpty(name);
		Check.notNull(size);
		modes = List.copyOf(modes);
	}

	/**
	 * Retrieves the current display mode for this monitor.
	 * @param desktop Desktop service
	 * @return Current display mode
	 */
	public DisplayMode mode(Desktop desktop) {
		final DesktopDisplayMode mode = desktop.library().glfwGetVideoMode(this);
		return DisplayMode.of(mode);
	}

	/**
	 * Enumerates the monitors attached to this system.
	 * @param desktop Desktop service
	 * @return Monitors
	 */
	public static List<Monitor> monitors(Desktop desktop) {
		// Enumerate monitors
		final DesktopLibraryMonitor lib = desktop.library();
		final ReferenceFactory factory = desktop.factory();
		final IntByReference count = factory.integer();
		final Pointer[] monitors = lib.glfwGetMonitors(count).getPointerArray(0, count.getValue());

		// Create monitors
		final Function<Pointer, Monitor> create = ptr -> {
			// Retrieve monitor name
			final String name = lib.glfwGetMonitorName(ptr);

			// Retrieve monitor dimensions
			final IntByReference w = factory.integer();
			final IntByReference h = factory.integer();
			lib.glfwGetMonitorPhysicalSize(ptr, w, h);

			// Retrieve display modes
			final DesktopDisplayMode first = lib.glfwGetVideoModes(ptr, count);
			final DesktopDisplayMode[] array = (DesktopDisplayMode[]) first.toArray(count.getValue());
			final List<DisplayMode> modes = Arrays.stream(array).map(DisplayMode::of).toList();

			// Create monitor
			return new Monitor(new Handle(ptr), name, new Dimensions(w.getValue(), h.getValue()), modes);
		};

		// Create monitors
		return Arrays
				.stream(monitors)
				.map(create)
				.toList();
	}
}
