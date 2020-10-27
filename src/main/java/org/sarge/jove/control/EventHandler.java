package org.sarge.jove.control;

public interface EventHandler {
	void handle(Position event);
	void handle(Button.Event event);
	void handle(Axis.Event event);
}
