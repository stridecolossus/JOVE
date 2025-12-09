package org.sarge.jove.platform.desktop;

import org.sarge.jove.platform.desktop.WindowTest.MockWindowLibrary;

public class MockDeviceLibrary extends MockWindowLibrary implements DeviceLibrary {
	@Override
	public void glfwPollEvents() {
	}

	@Override
	public String glfwGetKeyName(int key, int scancode) {
		return null;
	}

	@Override
	public void glfwSetKeyCallback(Window window, KeyListener listener) {
	}

	@Override
	public void glfwSetCursorPosCallback(Window window, MouseListener listener) {
	}

	@Override
	public void glfwSetScrollCallback(Window window, MouseListener listener) {
	}

	@Override
	public void glfwSetMouseButtonCallback(Window window, MouseButtonListener listener) {
	}
}
