package org.sarge.jove.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.Window.Descriptor;

public class WindowTest {
	private Descriptor props;
	private Monitor monitor;

	@BeforeEach
	public void before() {
		monitor = new Monitor(new Object(), "monitor", new Dimensions(1, 2), List.of(new Monitor.DisplayMode(new Dimensions(3, 4), new int[]{1, 2, 3}, 42)));
		props = new Descriptor.Builder()
			.title("title")
			.size(new Dimensions(5, 6))
			.monitor(monitor)
			.build();
	}

	@Test
	public void constructor() {
		assertEquals("title", props.title());
		assertEquals(new Dimensions(5, 6), props.size());
		assertEquals(Optional.of(monitor), props.monitor());
	}
}
