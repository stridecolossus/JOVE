package org.sarge.jove.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Resource.Tracker;

public class ResourceTest {
	private Tracker<Resource> tracker;
	private Resource res;

	@BeforeEach
	public void before() {
		tracker = new Tracker<>();
		res = mock(Resource.class);
	}

	@Test
	public void constructor() {
		assertEquals(0, tracker.size());
		assertNotNull(tracker.stream());
		assertEquals(0, tracker.stream().count());
	}

	@Test
	public void add() {
		tracker.add(res);
		assertEquals(1, tracker.size());
		assertArrayEquals(new Resource[]{res}, tracker.stream().toArray());
	}

	@Test
	public void addDuplicate() {
		tracker.add(res);
		assertThrows(IllegalArgumentException.class, () -> tracker.add(res));
	}

	@Test
	public void remove() {
		tracker.add(res);
		tracker.remove(res);
		assertEquals(0, tracker.size());
		assertEquals(0, tracker.stream().count());
	}

	@Test
	public void removeNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> tracker.remove(res));
	}

	@Test
	public void destroy() {
		tracker.add(res);
		tracker.destroy();
		verify(res).destroy();
		assertEquals(0, tracker.size());
		assertEquals(0, tracker.stream().count());
	}
}
