package org.sarge.jove.material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.material.Material.Property;

public class FloatPropertyBinderTest {
	private Property.Binder binder;
	private Shader.Parameter param;

	@BeforeEach
	public void before() {
		binder = new FloatPropertyBinder(() -> 42f);
		param = mock(Shader.Parameter.class);
	}

	@Test
	public void apply() {
		binder.apply(param);
		verify(param).set(42f);
	}
}
