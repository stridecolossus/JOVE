package org.sarge.jove.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Resource.Handle;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.Resource.Tracker;

import com.sun.jna.Pointer;

public class ResourceTest {
	@Nested
	class HandleTests {
		private static final String HANDLE = "handle";

		private Handle<String> handle;

		@BeforeEach
		public void before() {
			handle = new Handle<>(HANDLE);
		}

		@Test
		public void constructor() {
			assertEquals(HANDLE, handle.handle());
			assertEquals(false, handle.isDestroyed());
		}

		@Test
		public void handleDestroyed() {
			handle.destroy();
			assertThrows(IllegalStateException.class, () -> handle.handle());
		}

		@Test
		public void destroy() {
			handle.destroy();
			assertEquals(true, handle.isDestroyed());
		}

		@Test
		public void destroyAlreadyDestroyed() {
			handle.destroy();
			assertThrows(IllegalStateException.class, () -> handle.destroy());
		}

		@Test
		public void pointerHandle() {
			final Pointer ptr = mock(Pointer.class);
			final PointerHandle handle = new PointerHandle(ptr);
			assertEquals(ptr, handle.handle());
		}
	}

	@Nested
	class TrackerTests {
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
}
