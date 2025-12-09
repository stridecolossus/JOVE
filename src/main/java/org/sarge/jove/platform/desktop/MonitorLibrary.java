package org.sarge.jove.platform.desktop;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;

/**
 * GLFW monitor API.
 * @author Sarge
 */
interface MonitorLibrary {
	/**
	 * GLFW display mode.
	 */
	class DesktopDisplayMode implements NativeStructure {
		public int width;
		public int height;
		public int red;
		public int green;
		public int blue;
		public int refresh;

		@Override
		public GroupLayout layout() {
			return MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height"),
					JAVA_INT.withName("red"),
					JAVA_INT.withName("green"),
					JAVA_INT.withName("blue"),
					JAVA_INT.withName("refresh")
			);
		}
	}

	/**
	 * Enumerates monitors attached to this system.
	 * @param count Returned number of monitors
	 * @return Pointer to array of monitors
	 */
	Handle glfwGetMonitors(IntegerReference count);

	/**
	 * Retrieves the current video mode of the given monitor.
	 * @param monitor Monitor
	 * @return Current display mode of the given monitor
	 */
	Handle glfwGetVideoMode(Monitor monitor);
	// TODO - this is actually a POINTER to a structure, we need to differentiate this!
	//DesktopDisplayMode glfwGetVideoMode(Monitor monitor);

	/**
	 * Enumerates the video modes supported by the given monitor.
	 * @param monitor		Monitor
	 * @param count			Number of modes
	 * @return Display modes for the given monitor
	 */
	Handle glfwGetVideoModes(Handle monitor, IntegerReference count);

	/**
	 * Retrieves the dimensions of the given monitor.
	 * @param monitor		Monitor
	 * @param w				Returned width
	 * @param h				Returned height
	 */
	void glfwGetMonitorPhysicalSize(Handle monitor, IntegerReference w, IntegerReference h);

	/**
	 * Retrieves the name of the given monitor.
	 * @param monitor Monitor
	 * @return Monitor name
	 */
	String glfwGetMonitorName(Handle monitor);
}
