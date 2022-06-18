package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.PositionEvent.Handler;

public class PositionEventTest {
	private Source<PositionEvent> source;
	private PositionEvent event;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		source = mock(Source.class);
		event = new PositionEvent(source, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(source, event.source());
		assertEquals(2, event.x());
		assertEquals(3, event.y());
	}

	@Test
	void equals() {
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(new PositionEvent(source, 2, 3)));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(new PositionEvent(source, 3, 4)));
	}

	@Test
	void adapter() {
		final Handler handler = mock(Handler.class);
		final Consumer<PositionEvent> adapter = Handler.adapter(handler);
		adapter.accept(event);
		verify(handler).handle(2, 3);
	}
}
