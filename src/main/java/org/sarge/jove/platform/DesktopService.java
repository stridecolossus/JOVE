package org.sarge.jove.platform;

import java.util.List;

import org.sarge.jove.common.Handle;

import com.sun.jna.Pointer;

/**
 * A <i>desktop service</i> abstracts the windowing system of the platform.
 * @author Sarge
 */
public interface DesktopService extends Service {
	/**
	 * Retrieves the monitors attached to this system.
	 * The <i>primary</i> monitor is the first in the list.
	 * @return Monitors attached to this system
	 */
	List<Monitor> monitors();

	/**
	 * Retrieves the current display mode for the given monitor.
	 * @param monitor Monitor
	 * @return Display mode
	 */
	Monitor.DisplayMode mode(Monitor monitor);

	/**
	 * @return Whether Vulkan is supported by this platform
	 */
	boolean isVulkanSupported();

	/**
	 * @return Required Vulkan extensions
	 */
	String[] extensions();

	/**
	 * Creates a new window.
	 * @param descriptor Window descriptor
	 * @return New window
	 */
	Window window(Window.Descriptor descriptor);

	/**
	 * Creates a Vulkan surface for the given window.
	 * @param vulkan		Vulkan instance
	 * @param window		Window
	 * @return Surface handle
	 */
	Pointer surface(Handle vulkan, Handle window);
	// TODO
	// - surface class?
	// - accepts actual Window object? => Window extends Handle (except Window is an interface!)
}
