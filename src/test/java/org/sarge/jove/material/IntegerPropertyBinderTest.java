package org.sarge.jove.material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.material.Material.Property;

public class IntegerPropertyBinderTest {
	private Property.Binder binder;
	private Shader.Parameter param;

	@BeforeEach
	public void before() {
		binder = new IntegerPropertyBinder(() -> 42);
		param = mock(Shader.Parameter.class);
	}

	@Test
	public void apply() {
		binder.apply(param);
		verify(param).set(42);
	}

	@Test
	public void bool() {
		binder = IntegerPropertyBinder.of(() -> true);
		binder.apply(param);
		verify(param).set(1);
	}
}
