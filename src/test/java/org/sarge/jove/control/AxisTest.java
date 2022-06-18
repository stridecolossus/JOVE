package org.sarge.jove.control;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Axis.Handler;

public class AxisTest {
	private Axis axis;

	@BeforeEach
	void before() {
		axis = mock(Axis.class);
		when(axis.value()).thenReturn(3f);
	}

	@Test
	void adapter() {
		final Handler handler = mock(Handler.class);
		final Consumer<Axis> adapter = Handler.adapter(handler);
		adapter.accept(axis);
		verify(handler).handle(3f);
	}
}
