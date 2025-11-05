package org.sarge.jove.platform.desktop;

import org.sarge.jove.foreign.IntegerReference;

/**
 * GLFW monitor API.
 * @author Sarge
 */
interface MonitorLibrary {
	/**
	 * GLFW display mode.
	 */
	class DesktopDisplayMode {
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
	//ArrayReturnValue<Monitor>
	Object glfwGetMonitors(IntegerReference count);

	/**
	 * Enumerates the video modes supported by the given monitor.
	 * @param monitor		Monitor
	 * @param count			Number of modes
	 * @return Display modes for the given monitor
	 */
	DesktopDisplayMode glfwGetVideoModes(Monitor monitor, IntegerReference count);

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
	void glfwGetMonitorPhysicalSize(Monitor monitor, IntegerReference w, IntegerReference h);

	/**
	 * Retrieves the name of the given monitor.
	 * @param monitor Monitor
	 * @return Monitor name
	 */
	String glfwGetMonitorName(Monitor monitor);
}
