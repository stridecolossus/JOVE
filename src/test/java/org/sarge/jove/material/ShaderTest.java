package org.sarge.jove.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShaderTest {
	private Shader shader;
	private Shader.Parameter param;

	@BeforeEach
	public void before() {
		param = mock(Shader.Parameter.class);
		shader = new Shader(Map.of("param", param));
	}

	@Test
	public void parameters() {
		assertEquals(Map.of("param", param), shader.parameters());
	}

	@Test
	public void parameter() {
		assertEquals(param, shader.parameter("param"));
		assertEquals(null, shader.parameter("cobblers"));
	}
}
