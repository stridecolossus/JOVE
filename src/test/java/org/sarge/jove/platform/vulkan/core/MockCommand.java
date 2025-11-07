package org.sarge.jove.platform.vulkan.core;

public class MockCommand implements Command {
	public Buffer buffer;

	@Override
	public void execute(Buffer buffer) {
		this.buffer = buffer;
	}
}
