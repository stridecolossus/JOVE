package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Handler;

public class InputEventTypeTest {
	@Test
	void of() {
		final Runnable code = mock(Runnable.class);
		final Handler handler = Handler.of(code);
		assertNotNull(handler);
		handler.accept(null);
		verify(code).run();
	}
}
