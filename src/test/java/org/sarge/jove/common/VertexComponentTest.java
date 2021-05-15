package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VertexComponentTest {
	private VertexComponent component;

	@BeforeEach
	void before() {
		component = spy(VertexComponent.class);
	}

	@Test
	void length() {
		final Layout layout = Layout.of(2);
		when(component.layout()).thenReturn(layout);
		assertEquals(layout.length(), component.length());
	}
}
