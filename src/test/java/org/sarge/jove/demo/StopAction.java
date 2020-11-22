package org.sarge.jove.demo;

import java.util.concurrent.atomic.AtomicBoolean;

import org.sarge.jove.control.Action.SimpleAction;

public class StopAction implements SimpleAction {
	private final AtomicBoolean running = new AtomicBoolean(true);

	public boolean isRunning() {
		return running.get();
	}

	@Override
	public void run() {
		running.set(false);
	}
}
