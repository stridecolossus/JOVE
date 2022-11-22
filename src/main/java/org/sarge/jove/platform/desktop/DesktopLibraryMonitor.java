package org.sarge.jove.platform.desktop;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.IntByReference;

/**
 * GLFW monitor API.
 * @author Sarge
 */
interface DesktopLibraryMonitor {
	/**
	 * GLFW display mode.
	 */
	@FieldOrder({"width", "height", "red", "green", "blue", "refresh"})
	class DesktopDisplayMode extends Structure {
		public int width;
		public int height;
		public int red;
		public int green;
		public int blue;
		public int refresh;
	}

	/**
	 * Enumerates monitors attached to this system.
	 * @param count Number of monitors
	 * @return Pointer to array of monitors
	 */
	Pointer glfwGetMonitors(IntByReference count);

	/**
	 * Enumerates the video modes supported by the given monitor.
	 * @param monitor		Monitor
	 * @param count			Number of modes
	 * @return Display modes for the given monitor
	 */
	DesktopDisplayMode glfwGetVideoModes(Pointer monitor, IntByReference count);

	/**
	 * Retrieves the current video mode of the given monitor.
	 * @param monitor Monitor
	 * @return Current display mode of the given monitor
	 */
	DesktopDisplayMode glfwGetVideoMode(Monitor monitor);

	/**
	 * Retrieves the dimensions of the given monitor.
	 * @param monitor		Monitor
	 * @param w				Returned width
	 * @param h				Returned height
	 */
	void glfwGetMonitorPhysicalSize(Pointer monitor, IntByReference w, IntByReference h);

	/**
	 * Retrieves the name of the given monitor.
	 * @param monitor Monitor
	 * @return Monitor name
	 */
	String glfwGetMonitorName(Pointer monitor);
}
