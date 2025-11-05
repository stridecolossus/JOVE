package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.InstanceTest.MockInstanceLibrary;

public class MockInstance extends Instance {
	public MockInstance() {
		super(new Handle(1), new MockInstanceLibrary());
	}
}
