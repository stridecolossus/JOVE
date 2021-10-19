package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;

/**
 * A <i>monitor</i> describes a physical monitor attached to this system.
 */
public final class Monitor implements NativeObject {
	/**
	 * Display mode.
	 */
	public static final class DisplayMode { // TODO - record
		private final Dimensions size;
		private final int[] bits;
		private final int refresh;

		/**
		 * Constructor.
		 * @param size			Size
		 * @param bits			RGB bit depth
		 * @param refresh		Refresh rate (Hz)
		 * @throws IllegalArgumentException if the given bit depth is not an RGB array
		 */
		public DisplayMode(Dimensions size, int[] bits, int refresh) {
			if(bits.length != 3) throw new IllegalArgumentException("Invalid RGB bit depth array");
			this.size = notNull(size);
			this.bits = Arrays.copyOf(bits, bits.length);
			this.refresh = oneOrMore(refresh);
		}

		/**
		 * @return Size
		 */
		public Dimensions size() {
			return size;
		}

		/**
		 * @return RGB bit depth
		 */
		public int[] depth() {
			return Arrays.copyOf(bits, bits.length);
		}

		/**
		 * @return Refresh rate (Hz)
		 */
		public int refresh() {
			return refresh;
		}
	}

	private final Handle handle;
	private final String name;
	private final Dimensions size;
	private final List<DisplayMode> modes;

	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param name		Monitor name
	 * @param size		Physical dimensions
	 * @param modes		Display modes supported by this monitor
	 */
	public Monitor(Handle handle, String name, Dimensions size, List<DisplayMode> modes) {
		this.handle = notNull(handle);
		this.name = notEmpty(name);
		this.size = notNull(size);
		this.modes = List.copyOf(modes);
	}

	/**
	 * @return Monitor handle
	 */
	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Monitor name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Physical dimensions of this monitor
	 */
	public Dimensions size() {
		return size;
	}

	/**
	 * @return Display modes supported by this monitor
	 */
	public List<DisplayMode> modes() {
		return modes;
	}
}


///**
//* @return Monitors on this desktop
//*/
//public List<Monitor> monitors() {
//	final IntByReference count = new IntByReference();
//	final Pointer[] monitors = instance.glfwGetMonitors(count).getPointerArray(0, count.getValue());
//	return Arrays.stream(monitors).map(this::monitor).collect(toList());
//}
//
///**
//* Retrieves and maps a monitor descriptor.
//*/
//private Monitor monitor(Pointer handle) {
//	// Lookup monitor dimensions
//	final IntByReference w = new IntByReference();
//	final IntByReference h = new IntByReference();
//	instance.glfwGetMonitorPhysicalSize(handle, w, h);
//
//	// Lookup display modes
//	final IntByReference count = new IntByReference();
//	final FrameworkDisplayMode result = instance.glfwGetVideoModes(handle, count);
//	final FrameworkDisplayMode[] array = (FrameworkDisplayMode[]) result.toArray(count.getValue());
//	final List<Monitor.DisplayMode> modes = Arrays.stream(array).map(Desktop::toDisplayMode).collect(toList());
//
//	// Create monitor
//	final String name = instance.glfwGetMonitorName(handle);
////	return new Monitor(handle, name, new Dimensions(w.getValue(), h.getValue()), modes);
//	return null;
//}
//
//public DisplayMode mode(Monitor monitor) {
////	final FrameworkDisplayMode mode = instance.glfwGetVideoMode((Pointer) monitor.handle());
////	return toDisplayMode(mode);
//	return null;
//}
//
///**
//* Maps a GLFW display mode.
//*/
//private static DisplayMode toDisplayMode(FrameworkDisplayMode mode) {
//	final int[] depth = new int[]{mode.red, mode.green, mode.blue};
//	return new Monitor.DisplayMode(new Dimensions(mode.width, mode.height), depth, mode.refresh);
//}
