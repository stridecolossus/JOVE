package org.sarge.jove.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.material.Material.Property;
import org.sarge.jove.material.Material.Property.Policy;

public class MaterialTest {
	private Material mat;
	private Property prop;
	private Shader shader;
	private Shader.Parameter param;

	@BeforeEach
	public void before() {
		param = mock(Shader.Parameter.class);
		shader = new Shader(Map.of("param", param));
		prop = new Property(new IntegerPropertyBinder(() -> 42), Policy.MANUAL);
		mat = new Material("mat", shader, Map.of("param", prop));
	}

	@Test
	public void constructor() {
		assertEquals("mat", mat.name());
		assertEquals(Optional.of(shader), mat.shader());
	}

	@Test
	public void property() {
		assertEquals(null, prop.parameter());
		assertEquals(Policy.MANUAL, prop.policy());
		assertNotNull(prop.binder());
		assertEquals(1, prop.binder().size());
	}

	@Test
	public void properties() {
		assertEquals(Map.of("param", prop), mat.properties());
	}

	@Test
	public void get() {
		assertEquals(prop, mat.property("param"));
		assertEquals(null, mat.property("cobblers"));
	}

	@Test
	public void bind() {
		mat.bind(shader::parameter);
		assertEquals(param, prop.parameter());
	}

	@Test
	public void bindAlreadyBound() {
		mat.bind(shader::parameter);
		assertThrows(IllegalStateException.class, () -> mat.bind(shader::parameter));
	}

	@Test
	public void bindUnknownParameter() {
		shader = new Shader(Map.of());
		assertThrows(IllegalStateException.class, () -> mat.bind(shader::parameter));
	}

	@Test
	public void apply() {
		mat.bind(shader::parameter);
		mat.apply(Policy.MANUAL);
		verify(param).set(42);
	}

	@Test
	public void applyNotBound() {
		assertThrows(IllegalStateException.class, () -> mat.apply(Policy.MANUAL));
	}

	@Test
	public void applyDifferentPolicy() {
		mat.bind(shader::parameter);
		mat.apply(Policy.NODE);
		verifyZeroInteractions(param);
	}

	@Test
	public void builder() {
		mat = new Material.Builder("mat")
			.shader(shader)
			.add("param", prop)
			.build();
		assertEquals("mat", mat.name());
		assertEquals(Optional.of(shader), mat.shader());
		assertEquals(Map.of("param", prop), mat.properties());
	}
}
