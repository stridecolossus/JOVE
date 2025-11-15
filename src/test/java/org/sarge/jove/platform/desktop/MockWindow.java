package org.sarge.jove.platform.desktop;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.desktop.WindowTest.MockWindowLibrary;

public class MockWindow extends Window {
	public MockWindow() {
		super(new Handle(1), new MockWindowLibrary());
	}
}
