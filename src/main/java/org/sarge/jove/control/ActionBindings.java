package org.sarge.jove.control;

import java.util.*;

import org.sarge.jove.platform.desktop.Device;

public class ActionBindings {
	//private final Map<Class<?>, V>

	public ActionBindings(List<Action<?>> actions) {

	}

	public Map<Action<?>, List<Device<?>>> actions() {
		return null;
	}

	public <E> void bind(Device<E> device, Action<E> action) {

	}

	public <E> void remove(Device<E> device, Action<E> action) {

	}

	public <E> void remove(Action<E> action) {

	}

	public void clear() {

	}

	public void accept(Object event) {

	}
}
