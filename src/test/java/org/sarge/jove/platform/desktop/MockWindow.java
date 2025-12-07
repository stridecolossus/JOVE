package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.desktop.WindowTest.MockWindowLibrary;

public class MockWindow extends Window {
	public MockWindow() {
		this(new MockWindowLibrary());
	}

	public MockWindow(WindowLibrary library) {
		super(new Handle(1), library);
	}
}
