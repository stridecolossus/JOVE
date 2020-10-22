package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.desktop.WindowDescriptor.Property;

public class WindowDescriptorTest {
	private static final Dimensions SIZE = new Dimensions(640, 480);

	private WindowDescriptor descriptor;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		descriptor = new WindowDescriptor("title", SIZE, null, Set.of(Property.DECORATED));
		lib = mock(DesktopLibrary.class);
	}

	@Test
	void constructor() {
		assertEquals("title", descriptor.title());
		assertEquals(SIZE, descriptor.size());
		assertEquals(Optional.empty(), descriptor.monitor());
		assertEquals(Set.of(Property.DECORATED), descriptor.properties());
	}

	@Test
	void property() {
		Property.DECORATED.apply(lib);
		verify(lib).glfwWindowHint(0x00020005, 1);
	}

	@Test
	void disableContext() {
		Property.DISABLE_OPENGL.apply(lib);
		verify(lib).glfwWindowHint(0x00022001, 0);
	}

	@Test
	void builder() {
		final WindowDescriptor result = new WindowDescriptor.Builder()
				.title("title")
				.size(SIZE)
				.property(Property.DECORATED)
				.build();

		assertEquals(descriptor, result);
	}
}
