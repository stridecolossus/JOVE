package org.sarge.jove.scene;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.scene.Node.Visitor;

@SuppressWarnings({"rawtypes", "unchecked"})
public class InheritedPropertyTest {
	private InheritedProperty prop;

	@BeforeEach
	void before() {
		prop = mock(InheritedProperty.class);
	}

	@Test
	void visitor() {
		final Visitor visitor = InheritedProperty.visitor(node -> prop);
		when(prop.isDirty()).thenReturn(true);
		visitor.visit(null);
		verify(prop).update(null);
	}
}
