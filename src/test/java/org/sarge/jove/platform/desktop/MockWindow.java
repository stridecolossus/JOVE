package org.sarge.jove.platform.desktop;

import java.util.Map;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Callback;
import org.sarge.jove.platform.desktop.WindowTest.MockWindowLibrary;

public class MockWindow extends Window {
	public MockWindow() {
		this(new MockWindowLibrary());
	}

	public MockWindow(WindowLibrary library) {
		super(new Handle(1), library);
	}

	@Override
	public Map<Object, Callback> listeners() {
		return super.listeners();
	}
}
