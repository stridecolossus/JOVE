package org.sarge.jove.platform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.Monitor.DisplayMode;

public class MonitorTest {
	private Monitor monitor;
	private DisplayMode mode;
	private Object handle;

	@BeforeEach
	public void before() {
		mode = new Monitor.DisplayMode(new Dimensions(3, 4), new int[]{1, 2, 3}, 60);
		handle = new Object();
		monitor = new Monitor(handle, "name", new Dimensions(1, 2), List.of(mode));
	}

	@Test
	public void constructor() {
		assertEquals(handle, monitor.handle());
		assertEquals("name", monitor.name());
		assertEquals(new Dimensions(1, 2), monitor.size());
		assertEquals(List.of(mode), monitor.modes());
	}

	@Test
	public void mode() {
		assertEquals(new Dimensions(3, 4), mode.size());
		assertArrayEquals(new int[]{1, 2, 3}, mode.depth());
		assertEquals(60, mode.refresh());
	}

	@Test
	public void modeInvalidBitDepth() {
		assertThrows(IllegalArgumentException.class, () -> new Monitor.DisplayMode(new Dimensions(3, 4), new int[]{1}, 42));
	}
}
