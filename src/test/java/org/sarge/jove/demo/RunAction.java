package org.sarge.jove.demo;

import org.sarge.jove.control.Action.SimpleAction;

public class RunAction implements SimpleAction {
	private boolean running = true;

	public boolean isRunning() {
		return running;
	}

	@Override
	public void run() {
		running = false;
	}
}
